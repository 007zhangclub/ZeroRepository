package com.bjpowernode.crm.settings.dao;

import com.bjpowernode.crm.settings.domain.DictionaryType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DictionaryTypeDao {

    @Select("select * from tbl_dic_type")
    List<DictionaryType> findAll();

    @Select("select * from tbl_dic_type where code = #{code}")
    DictionaryType findOne(String code);

    @Insert("insert into tbl_dic_type (code,name,description) values (#{code},#{name},#{description})")
    int insert(DictionaryType dictionaryType);

    int update(DictionaryType dictionaryType);

    int deleteList(List<String> deleteList);
}
