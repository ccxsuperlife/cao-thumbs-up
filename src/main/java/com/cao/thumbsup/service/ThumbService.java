package com.cao.thumbsup.service;

import com.cao.thumbsup.model.dto.thumb.DoThumbRequest;
import com.cao.thumbsup.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.thumbsup.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author baogondian
* @description 针对表【thumb】的数据库操作Service
* @createDate 2025-04-28 13:51:59
*/
public interface ThumbService extends IService<Thumb> {


    /**
     * 点赞
     * @param doThumbRequest
     * @param request
     * @return
     */
    public boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);


    /**
     * 取消点赞
     * @param doThumbRequest
     * @param request
     * @return
     */
    public boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    /**
     * 判断用户是否已经点赞
     * @param blogId
     * @param userId
     * @return
     */
    Boolean hasThumb(Long blogId, Long userId);

}
