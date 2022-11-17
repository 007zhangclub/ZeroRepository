package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.ClueActivityRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClueActivityRelationDao {


    @Select("select * from tbl_clue_activity_relation where clueId = #{clueId}")
    List<ClueActivityRelation> findList(String clueId);

    @Delete("delete from tbl_clue_activity_relation where clueId = #{clueId}")
    int deleteByClueId(String clueId);
}
