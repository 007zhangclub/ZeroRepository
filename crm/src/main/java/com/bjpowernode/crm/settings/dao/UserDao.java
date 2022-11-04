package com.bjpowernode.crm.settings.dao;

import com.bjpowernode.crm.settings.domain.User;
import org.apache.ibatis.annotations.Select;

public interface UserDao {
    @Select("select * from tbl_user where loginAct = #{loginAct}")
    User findUserByLoginAct(String loginAct);
}
