package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.ClueRemark;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClueRemarkDao {
    @Select("select * from tbl_clue_remark where clueId = #{clueId}")
    List<ClueRemark> findList(String clueId);

    int insert(ClueRemark clueRemark);

    @Delete("delete from tbl_clue_remark where clueId = #{clueId}")
    int deleteByClueId(String clueId);
}
