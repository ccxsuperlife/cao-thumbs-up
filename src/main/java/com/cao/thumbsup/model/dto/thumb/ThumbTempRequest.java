package com.cao.thumbsup.model.dto.thumb;

import com.cao.thumbsup.model.enums.ThumbTypeEnum;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * @author 小曹同学
 * @date 2025/5/23
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class ThumbTempRequest {
    /**
     * 类型
     * {@link ThumbTypeEnum }
     */
    private Integer type;

    /**
     * 点赞/取消点赞时间
     */
    private String time;

}
