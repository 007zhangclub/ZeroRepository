package com.bjpowernode.crm.settings.dao;

import com.bjpowernode.crm.settings.domain.DictionaryValue;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DictionaryValueDao {
    @Select("select * from tbl_dic_value where typeCode = #{code}")
    List<DictionaryValue> findList(String code);
}
