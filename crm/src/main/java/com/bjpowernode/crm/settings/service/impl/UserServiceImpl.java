package com.bjpowernode.crm.settings.service.impl;

import com.bjpowernode.crm.settings.dao.UserDao;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;


    @Override
    public User findUserByLoginAct(String loginAct, String loginPwd) {

        //根据用户名查询用户对象
        User user = userDao.findUserByLoginAct(loginAct);

        //校验用户是否存在
        if(ObjectUtils.isEmpty(user))
            //抛出异常,代码不再向下进行
            throw new RuntimeException("用户查询异常");

        //根据用户的密码进行比对
        //参数1,明文密码(前端传递过来的),参数2,密文密码(数据库查询出来的)
        boolean matches = new BCryptPasswordEncoder().matches(loginPwd, user.getLoginPwd());

        if(!matches)
            throw new RuntimeException("用户名或密码错误");

        //用户登录成功
        return user;
    }
}
