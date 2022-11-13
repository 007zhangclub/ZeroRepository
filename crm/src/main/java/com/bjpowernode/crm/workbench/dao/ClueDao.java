package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Clue;
import org.apache.ibatis.annotations.Insert;

public interface ClueDao {
    int insert(Clue clue);
}
