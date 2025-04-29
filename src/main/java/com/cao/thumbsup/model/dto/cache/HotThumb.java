package com.cao.thumbsup.model.dto.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 小曹同学
 * @date 2025/4/29
 */
@Schema(description = "热门点赞缓存")
@Data
public class HotThumb implements Serializable {


    @Serial
    private static final long serialVersionUID = 5512737871676466506L;

    @Schema(description = "点赞id")
    private Long ThumbId;
    @Schema(description = "点赞过期时间")
    private Long expireTime;


}
