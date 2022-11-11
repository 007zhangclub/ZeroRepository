package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ActivityRemarkDao {
    @Select("select * from tbl_activity_remark where activityId = #{activityId}")
    List<ActivityRemark> findList(String activityId);
}
