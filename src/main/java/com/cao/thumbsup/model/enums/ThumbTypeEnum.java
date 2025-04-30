package com.cao.thumbsup.model.enums;

import lombok.Getter;

/**
 * @author 小曹同学
 * @date 2025/4/30
 */
@Getter
public enum ThumbTypeEnum {

    // 点赞
    INCR(1),
    // 取消点赞
    DECR(-1),
    // 无变化
    NON(0);


    private final int value;

    ThumbTypeEnum(int value) {
        this.value = value;
    }
}
