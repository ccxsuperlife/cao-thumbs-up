package com.cao.thumbsup.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.constant.RedisLuaScriptConstant;
import com.cao.thumbsup.exception.BusinessException;
import com.cao.thumbsup.exception.ErrorCode;
import com.cao.thumbsup.mapper.ThumbMapper;
import com.cao.thumbsup.model.dto.cache.HotThumb;
import com.cao.thumbsup.model.dto.thumb.DoThumbRequest;
import com.cao.thumbsup.model.entity.Blog;
import com.cao.thumbsup.model.entity.Thumb;
import com.cao.thumbsup.model.entity.User;
import com.cao.thumbsup.model.enums.LuaStatusEnum;
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

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;

/**
 * @author baogondian
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-28 13:51:59
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {


    private final UserService userService;

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
        Long blogId = doThumbRequest.getBlogId();
        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId);
        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
        }
        return LuaStatusEnum.SUCCESS.getValue() == result;
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
        Long blogId = doThumbRequest.getBlogId();
        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // 执行Lua脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId);

        if(LuaStatusEnum.FAIL.getValue() == result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
        }
        return LuaStatusEnum.SUCCESS.getValue() == result;

    }


    private static String getTimeSlice() {
        Date nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    /**
     * 判断用户是否已点赞
     *
     * @param blogId
     * @param userId
     * @return
     */
    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }
}




