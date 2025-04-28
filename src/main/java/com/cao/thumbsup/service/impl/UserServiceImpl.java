package com.cao.thumbsup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.thumbsup.constant.UserConstant;
import com.cao.thumbsup.model.entity.User;
import com.cao.thumbsup.service.UserService;
import com.cao.thumbsup.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author baogondian
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-04-28 13:51:59
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }
}




