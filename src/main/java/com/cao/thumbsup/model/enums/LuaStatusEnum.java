package com.cao.thumbsup.model.enums;

import lombok.Getter;

/**
 * @author 小曹同学
 * @date 2025/4/30
 */
@Getter
public enum LuaStatusEnum {

    // 成功
    SUCCESS(1L),
    // 失败
    FAIL(-1L);

    private final long value;

    LuaStatusEnum(long value) {
        this.value = value;
    }
}
