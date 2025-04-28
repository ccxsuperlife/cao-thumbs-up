package com.cao.thumbsup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.exception.BusinessException;
import com.cao.thumbsup.exception.ErrorCode;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
                boolean exists = this.lambdaQuery().eq(Thumb::getBlogId, blogId).eq(Thumb::getUserId, loginUser.getId()).exists();
                if (exists) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
                }
                boolean update = blogService.lambdaUpdate().eq(Blog::getId, blogId).setSql("thumbCount = thumbCount + 1").update();
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                // 更新成功才执行
                return this.save(thumb) && update;
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
                Thumb thumb = this.lambdaQuery().eq(Thumb::getBlogId, blogId).eq(Thumb::getUserId, loginUser.getId()).one();
                if (thumb == null) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                }
                boolean update = blogService.lambdaUpdate().eq(Blog::getId, blogId).setSql("thumbCount = thumbCount - 1").update();
                // 更新成功才执行
                return this.removeById(thumb.getId()) && update;
            });
        }
        return result;
    }
}




