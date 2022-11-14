package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Activity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ActivityDao {
    List<Activity> findPage(@Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize, @Param("name") String name, @Param("owner") String owner, @Param("startDate") String startDate, @Param("endDate") String endDate);

    int findPageCount(@Param("name") String name, @Param("owner") String owner, @Param("startDate") String startDate, @Param("endDate") String endDate);

    int insert(Activity activity);

    Activity findById(String id);

    int update(Activity activity);

    int updateIsDelete(Activity activity);

    int insertList(List<Activity> activityList);

    List<Activity> findAll(@Param("ids") List<String> ids);

    @Select("select a.*,u.name as username from tbl_activity a left join tbl_user u on a.owner = u.id where a.isDelete = '0'")
    List<Activity> findList();
}
