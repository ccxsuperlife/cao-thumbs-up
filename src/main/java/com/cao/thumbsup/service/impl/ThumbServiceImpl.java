package com.cao.thumbsup.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.cao.thumbsup.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@Service(value = "thumbServiceDB")
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
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
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
                //获取博客的发布时间
                Blog blog = blogService.lambdaQuery().eq(Blog::getId, blogId).select(Blog::getCreateTime).one();
                long blogCreateTime = blog.getCreateTime().getTime();
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                boolean success = this.save(thumb) && update;
                // 点赞记录写入Redis
                if (success) {
                    HotThumb hotThumb = new HotThumb();
                    hotThumb.setThumbId(thumb.getId());
                    //热点点赞数据的过期时间设置为博客发布时间 + 30天
                    hotThumb.setExpireTime(blogCreateTime + LocalDateTimeUtil.now().plusDays(30).toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
                    redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), hotThumb);
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
                String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
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
                // 更新点赞数
                boolean update = blogService.lambdaUpdate().eq(Blog::getId, blogId).setSql("thumbCount = thumbCount - 1").update();
                // 删除点赞记录
                boolean success = this.removeById(hotThumb.getThumbId()) && update;
                // 将点赞记录从Redis中删除
                if (success) {
                    redisTemplate.opsForHash().delete(userThumbKey, blogId.toString());
                }
                // 更新成功才执行
                return true;
            });
        }
        return result;
    }

    /**
     * 判断用户是否已点赞
     * 1. 根据thumb:userId,blogId查询redis中是否有缓存数据
     * 2. 如果没有缓存数据,则从MySQL中查询
     * 3. 如果有缓存数据,则判断是否过期
     * 4. 如果过期则删除缓存数据,返回false
     * 5. 补充,之所以查询数据库是因为该数据是热点数据,存在过期时间,一旦过期,缓存中就没有了,所以需要从MySQL中查询
     *
     * @param blogId
     * @param userId
     * @return
     */
    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        String userThumbKey = RedisKeyUtil.getUserThumbKey(userId);
        //  从Redis中获取点赞记录
        Object hotThumbObj = redisTemplate.opsForHash().get(userThumbKey, blogId.toString());
        //  判断是否为null
        HotThumb hotThumb = (HotThumb) hotThumbObj;
        if (hotThumbObj == null) {
            // 从MySQL中获取点赞记录
            Thumb thumb = this.lambdaQuery().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId).one();
            return thumb != null;
        }
        // Redis 中有点赞记录查看是否过期
        if (hotThumb.getExpireTime() < DateUtil.current()) {
            // 点赞数据过期
            redisTemplate.opsForHash().delete(userThumbKey, blogId.toString());
            return false;
        }
        return true;
    }
}




