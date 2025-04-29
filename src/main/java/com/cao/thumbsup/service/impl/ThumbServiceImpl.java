package com.cao.thumbsup.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.constant.ThumbConstant;
import com.cao.thumbsup.exception.BusinessException;
import com.cao.thumbsup.exception.ErrorCode;
import com.cao.thumbsup.model.dto.cache.HotThumb;
import com.cao.thumbsup.model.dto.thumb.DoThumbRequest;
import com.cao.thumbsup.model.entity.Blog;
import com.cao.thumbsup.model.entity.Thumb;
import com.cao.thumbsup.model.entity.User;
import com.cao.thumbsup.service.BlogService;
import com.cao.thumbsup.service.ThumbService;
import com.cao.thumbsup.mapper.ThumbMapper;
import com.cao.thumbsup.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZoneOffset;

/**
 * @author baogondian
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-28 13:51:59
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String lockKey = loginUser.getId().toString().intern();
        Boolean result;
        synchronized (lockKey) {
            result = transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
//                boolean exists = this.lambdaQuery().eq(Thumb::getBlogId, blogId).eq(Thumb::getUserId, loginUser.getId()).exists();
                // 将是否已点赞的逻辑改为从MySQL判断到Redis判断
                Boolean exists = this.hasThumb(blogId, loginUser.getId());
                if (exists) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
                }
                boolean update = blogService.lambdaUpdate().eq(Blog::getId, blogId).setSql("thumbCount = thumbCount + 1").update();
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                boolean success = this.save(thumb) && update;
                // 点赞记录写入Redis
                if (success) {
                    HotThumb hotThumb = new HotThumb();
                    hotThumb.setThumbId(thumb.getId());
                    //热点点赞数据的过期时间设置为30天
                    hotThumb.setExpireTime(LocalDateTimeUtil.now().plusDays(30).toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    String userThumbKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString();
                    redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), hotThumb);
//                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString(), thumb.getId());
                }
                // 更新成功才执行
                return true;
            });
        }
        return result;
    }

    @Override
    public boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String lockKey = loginUser.getId().toString().intern();
        Boolean result;
        synchronized (lockKey) {
            result = transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
//                Thumb thumb = this.lambdaQuery().eq(Thumb::getBlogId, blogId).eq(Thumb::getUserId, loginUser.getId()).one();
                // 将是否已点赞的逻辑改为从MySQL判断到Redis判断
                String userThumbKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString();
                Object objectThumb = redisTemplate.opsForHash().get(userThumbKey, blogId.toString());
                HotThumb hotThumb = (HotThumb) objectThumb;
                // redis中没有数据,或者已过期就去查询数据库
                if (hotThumb == null || hotThumb.getExpireTime() < DateUtil.current()) {
                    Thumb thumb = this.lambdaQuery().eq(Thumb::getBlogId, blogId).eq(Thumb::getUserId, loginUser.getId()).one();
                    if (thumb == null) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                    }
                    hotThumb = new HotThumb();
                    hotThumb.setThumbId(thumb.getId());

                }
                boolean update = blogService.lambdaUpdate().eq(Blog::getId, blogId).setSql("thumbCount = thumbCount - 1").update();
                boolean success = this.removeById(hotThumb.getThumbId()) && update;
                // 将点赞记录从Redis中删除
                if (success) {
                    redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                }
                // 更新成功才执行
                return true;
            });
        }
        return result;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        //  从Redis中获取点赞记录
        Object hotThumbObj = redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId.toString(), blogId.toString());
        //  判断是否为null
        HotThumb hotThumb = (HotThumb) hotThumbObj;
        if (hotThumbObj == null) {
            //   从MySQL中获取点赞记录
            Thumb thumb = this.lambdaQuery().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId).one();
            return thumb != null;
        }
        // Redis 中有点赞记录查看是否过期
        if (hotThumb.getExpireTime() < DateUtil.current()) {
            // 点赞数据过期
            redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
            return false;
        }
        return true;
    }

}




