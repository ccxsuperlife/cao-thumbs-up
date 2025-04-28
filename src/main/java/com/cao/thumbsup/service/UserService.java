package com.cao.thumbsup.service;

import com.cao.thumbsup.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author baogondian
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-28 13:51:59
*/
public interface UserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);
}
