package com.cao.thumbsup.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.constant.ThumbConstant;
import com.cao.thumbsup.constant.UserConstant;
import com.cao.thumbsup.model.entity.Blog;
import com.cao.thumbsup.model.entity.User;
import com.cao.thumbsup.model.vo.BlogVO;
import com.cao.thumbsup.service.BlogService;
import com.cao.thumbsup.mapper.BlogMapper;
import com.cao.thumbsup.service.ThumbService;
import com.cao.thumbsup.service.UserService;
import com.cao.thumbsup.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author baogondian
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2025-04-28 13:51:59
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public BlogVO getBlogVOById(Long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
        if (loginUser == null) {
            return blogVO;
        }
        // 判断用户是否点赞了该博客,改为从Redis中获取
//        Thumb thumb = thumbService.lambdaQuery().eq(Thumb::getBlogId, blog.getId()).eq(Thumb::getUserId, loginUser.getId()).one();
        Boolean exists = thumbService.hasThumb(blogId, loginUser.getId());
        //  设置用户是否点赞, 如果为空则表示未点赞, 如果不为空则表示已点赞
        blogVO.setHasThumb(exists);
        return blogVO;
    }

    @Override
    public List<BlogVO> getBlogVOByList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(loginUser)) {
            List<Object> blogIds = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            // 获取用户点赞的博客列表的逻辑从MySQL判断到Redis判断
//            List<Thumb> thumbList = thumbService.lambdaQuery().eq(Thumb::getUserId, loginUser.getId()).in(Thumb::getBlogId, blogIdSet).list();
            String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(userThumbKey, blogIds);
            for (int i = 0; i < thumbList.size(); i++) {
                if (thumbList.get(i) == null) {
                    continue;
                }
                blogIdHasThumbMap.put(Long.valueOf(blogIds.get(i).toString()), true);
            }

        }
        return blogList.stream().map(blog -> {
            BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
            blogVO.setHasThumb(blogIdHasThumbMap.containsValue(blogIdHasThumbMap.get(blog.getId())));
            return blogVO;
        }).toList();
    }
}




