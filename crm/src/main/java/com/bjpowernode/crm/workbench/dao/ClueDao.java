package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Clue;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface ClueDao {
    int insert(Clue clue);

    @Select("select c.*,u.name as username from tbl_clue c left join tbl_user u on c.owner = u.id where c.id = #{id}")
    Clue findById(String id);
}
