package com.cao.thumbsup.service;

import com.cao.thumbsup.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.thumbsup.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author baogondian
* @description 针对表【blog】的数据库操作Service
* @createDate 2025-04-28 13:51:59
*/
public interface BlogService extends IService<Blog> {


    /**
     * 获取博客详情
     * @param blogId
     * @param request
     * @return
     */
    BlogVO getBlogVOById(Long blogId, HttpServletRequest request);


    /**
     * 获取博客列表
     * @param blogList
     * @param request
     * @return
     */
    List<BlogVO> getBlogVOByList(List<Blog> blogList,HttpServletRequest request);

}
