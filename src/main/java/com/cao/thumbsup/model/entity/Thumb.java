package com.cao.thumbsup.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName thumb
 */
@TableName(value ="thumb")
@Data
public class Thumb implements Serializable {

    private static final long serialVersionUID = 5047393636330195955L;
    /**
     * 点赞id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 博客id
     */
    private Long blogId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;
}