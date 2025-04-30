package com.cao.thumbsup.constant;

/**
 * 点赞常量
 */
public interface ThumbConstant {
    //用户点赞key前缀
    String USER_THUMB_KEY_PREFIX = "thumb:";

    //
    String TEMP_THUMB_KEY_PREFIX = "temp:thumb:%s";

    @Deprecated
    //博客创建时间前缀
    String BLOG_CREATE_TIME_KEY_PREFIX = "blog:create_time";

    @Deprecated
    //博客缓存key前缀
    String BLOG_CACHE_KEY_PREFIX = "blog:cache:";

}
