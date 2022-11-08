package com.bjpowernode.crm.settings.dao;

import com.bjpowernode.crm.settings.domain.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserDao {
    @Select("select * from tbl_user where loginAct = #{loginAct}")
    User findUserByLoginAct(String loginAct);

    @Select("select * from tbl_user where loginAct = #{loginAct} and loginPwd = #{loginPwd}")
    User findUserByLoginActAndLoginPwd(@Param("loginAct") String loginAct, @Param("loginPwd") String loginPwd);

    @Select("select * from tbl_user")
    List<User> findAll();
}
