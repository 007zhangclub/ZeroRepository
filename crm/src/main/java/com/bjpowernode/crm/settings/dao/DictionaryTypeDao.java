package com.bjpowernode.crm.settings.dao;

import com.bjpowernode.crm.settings.domain.DictionaryType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DictionaryTypeDao {

    @Select("select * from tbl_dic_type")
    List<DictionaryType> findAll();
}
