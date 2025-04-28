package com.cao.thumbsup.model.vo;


import lombok.Data;

import java.util.Date;

/**
 * 视图包装类
 */
@Data
public class BlogVO {
    /**
     * 博客id
     */
    private Long id;

    /**
     * 博客标题
     */
    private String title;

    /**
     * 博客封面
     */
    private String coverImg;

    /**
     * 博客内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

}
