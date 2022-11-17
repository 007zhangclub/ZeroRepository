package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Contacts;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ContactsDao {

    @Select("select * from tbl_contacts where fullname = #{contactsName} and mphone = #{mphone}")
    Contacts findByFullName(@Param("contactsName") String contactsName, @Param("mphone") String mphone);

    int insert(Contacts contacts);
}
