package com.bjpowernode.crm.settings.dao;

import com.bjpowernode.crm.settings.domain.DictionaryValue;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DictionaryValueDao {
    @Select("select * from tbl_dic_value where typeCode = #{code}")
    List<DictionaryValue> findList(String code);

    @Select("select * from tbl_dic_value order by orderNo asc")
    List<DictionaryValue> findAll();

    @Insert("insert into tbl_dic_value (id,text,value,orderNo,typeCode) values (#{id},#{text},#{value},#{orderNo},#{typeCode})")
    int insert(DictionaryValue dictionaryValue);
}
