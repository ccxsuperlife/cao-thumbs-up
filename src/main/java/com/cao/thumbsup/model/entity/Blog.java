package com.cao.thumbsup.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName blog
 */
@TableName(value ="blog")
@Data
public class Blog implements Serializable {

    private static final long serialVersionUID = -2738999971956201283L;
    /**
     * 博客id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}