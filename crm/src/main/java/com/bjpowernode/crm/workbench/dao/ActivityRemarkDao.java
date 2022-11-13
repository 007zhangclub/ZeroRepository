package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ActivityRemarkDao {
    @Select("select * from tbl_activity_remark where activityId = #{activityId} order by editTime desc")
    List<ActivityRemark> findList(String activityId);

    int insert(ActivityRemark activityRemark);

    int update(ActivityRemark activityRemark);
}
