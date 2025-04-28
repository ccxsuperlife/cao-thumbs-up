package com.cao.thumbsup.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName thumb
 */
@TableName(value ="thumb")
@Data
public class Thumb {
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