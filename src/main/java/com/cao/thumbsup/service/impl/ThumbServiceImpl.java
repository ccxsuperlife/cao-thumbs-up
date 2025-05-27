package com.cao.thumbsup.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.constant.ThumbConstant;
import com.cao.thumbsup.exception.BusinessException;
import com.cao.thumbsup.exception.ErrorCode;
import com.cao.thumbsup.manager.cache.CacheManager;
import com.cao.thumbsup.mapper.ThumbMapper;
import com.cao.thumbsup.model.dto.cache.HotThumb;
import com.cao.thumbsup.model.dto.thumb.DoThumbRequest;
import com.cao.thumbsup.model.entity.Blog;
import com.cao.thumbsup.model.entity.Thumb;
import com.cao.thumbsup.model.entity.User;
import com.cao.thumbsup.service.BlogService;
import com.cao.thumbsup.service.ThumbService;
import com.cao.thumbsup.service.UserService;
import com.cao.thumbsup.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.constant.Constable;
import java.time.ZoneOffset;

/**
 * @author baogondian
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-28 13:51:59
 */
@Service(value = "thumbService")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final CacheManager cacheManager;

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
                Boolean exists = this.hasThumb(blogId, loginUser.getId());
                if (exists) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
                }
                boolean update = blogService.lambdaUpdate().
                        eq(Blog::getId, blogId).
                        setSql("thumbCount = thumbCount + 1").
                        update();
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                boolean success = this.save(thumb) && update;
                // 点赞记录写入Redis
                if (success) {
                    String hashKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
                    String filedKey = blogId.toString();
                    Long realThumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hashKey, filedKey, realThumbId);
                    cacheManager.putIfPresent(hashKey, filedKey, realThumbId);
                }
                // 更新成功才执行
                return success;
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
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String lockKey = loginUser.getId().toString().intern();
        Boolean result;
        synchronized (lockKey) {
            result = transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Object objectThumb = cacheManager.get(RedisKeyUtil.getUserThumbKey(loginUser.getId()), blogId.toString());
                if (objectThumb == null || objectThumb.equals(ThumbConstant.UN_THUMB_CONSTANT)) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                }
                Long thumbId = (Long) objectThumb;
                // 更新点赞数
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                // 删除点赞记录
                boolean success = this.removeById(thumbId) && update;
                // 将点赞记录从Redis中删除
                if (success) {
                    String hashKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
                    String filedKey = blogId.toString();
                    redisTemplate.opsForHash().delete(hashKey, filedKey);
                    cacheManager.putIfPresent(hashKey, filedKey, ThumbConstant.UN_THUMB_CONSTANT);
                }
                // 更新成功才执行
                return success;
            });
        }
        return result;
    }

    /**
     * 判断用户是否点赞该博客
     *
     * @param blogId
     * @param userId
     * @return
     */
    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        // 使用 CacheManager 从缓存中获取用户对博客的点赞状态
        Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
        // 如果缓存中没有数据，说明未点赞，返回 false
        if (thumbIdObj == null) {
            return false;
        }
        // 将缓存中的值转换为 Long 类型
        Long thumbId = (Long) thumbIdObj;
        // 判断是否是未点赞的标记值，如果不是，则表示已点赞
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }
}




