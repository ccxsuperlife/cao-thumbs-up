package com.cao.thumbsup.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.constant.UserConstant;
import com.cao.thumbsup.model.entity.Blog;
import com.cao.thumbsup.model.entity.Thumb;
import com.cao.thumbsup.model.entity.User;
import com.cao.thumbsup.model.vo.BlogVO;
import com.cao.thumbsup.service.BlogService;
import com.cao.thumbsup.mapper.BlogMapper;
import com.cao.thumbsup.service.ThumbService;
import com.cao.thumbsup.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author baogondian
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2025-04-28 13:51:59
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Override
    public BlogVO getBlogVOById(Long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
        if (loginUser == null) {
            return blogVO;
        }
        // 判断用户是否点赞了该博客
        Thumb thumb = thumbService.lambdaQuery().eq(Thumb::getBlogId, blog.getId()).eq(Thumb::getUserId, loginUser.getId()).one();
        //  设置用户是否点赞, 如果为空则表示未点赞, 如果不为空则表示已点赞
        blogVO.setHasThumb(thumb != null);
        return blogVO;
    }

    @Override
    public List<BlogVO> getBlogVOByList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(loginUser)) {
            Set<Long> blogIdSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
            List<Thumb> thumbList = thumbService.lambdaQuery().eq(Thumb::getUserId, loginUser.getId()).in(Thumb::getBlogId, blogIdSet).list();

            thumbList.forEach(thumb -> blogIdHasThumbMap.put(thumb.getBlogId(), true));

        }
        return blogList.stream().map(blog -> {
            BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
            blogVO.setHasThumb(blogIdHasThumbMap.containsValue(blogIdHasThumbMap.get(blog.getId())));
            return blogVO;
        }).toList();
    }
}




