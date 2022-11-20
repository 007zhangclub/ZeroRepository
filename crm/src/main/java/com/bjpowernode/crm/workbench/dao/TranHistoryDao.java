package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.TranHistory;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface TranHistoryDao {

    int insert(TranHistory history);

    List<TranHistory> findList(String tranId);

    @Select("select stage as name,count(id) as value from tbl_tran_history group by stage")
    List<Map<String, Object>> findChartData();
}
