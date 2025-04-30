package com.cao.thumbsup.util;

import com.cao.thumbsup.constant.ThumbConstant;

/**
 * @author 小曹同学
 * @date 2025/4/30
 */
public class RedisKeyUtil {

    /**
     * 获取用户点赞key
     * @param userId
     * @return
     */
    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId.toString();
    }

    /**
     * 获取临时点赞key
     * @param time
     * @return
     */
    public static String getTempThumbKey(String time) {
        return String.format(ThumbConstant.TEMP_THUMB_KEY_PREFIX, time);
    }

}
