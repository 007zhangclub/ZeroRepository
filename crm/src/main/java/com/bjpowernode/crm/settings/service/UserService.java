package com.bjpowernode.crm.settings.service;

import com.bjpowernode.crm.settings.domain.User;

import java.util.List;

public interface UserService {
    User findUserByLoginAct(String loginAct, String loginPwd, String ip);

    User findUserByLoginActAndLoginPwd(String loginAct, String loginPwd, String ip);

    List<User> findUserList();
}
