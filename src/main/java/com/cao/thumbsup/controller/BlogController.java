package com.cao.thumbsup.controller;


import com.cao.thumbsup.common.BaseResponse;
import com.cao.thumbsup.common.ResultUtils;
import com.cao.thumbsup.model.entity.Blog;
import com.cao.thumbsup.model.vo.BlogVO;
import com.cao.thumbsup.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {


    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public BaseResponse<BlogVO> getBlogVOById(Long blogId, HttpServletRequest request) {
        BlogVO blogVO = blogService.getBlogVOById(blogId, request);
        return ResultUtils.success(blogVO);
    }

    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> listBlogVOByList(HttpServletRequest request) {
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOList = blogService.getBlogVOByList(blogList, request);
        return ResultUtils.success(blogVOList);
    }
}
