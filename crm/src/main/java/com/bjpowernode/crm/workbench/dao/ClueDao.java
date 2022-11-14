package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.Clue;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClueDao {
    int insert(Clue clue);

    @Select("select c.*,u.name as username from tbl_clue c left join tbl_user u on c.owner = u.id where c.id = #{id}")
    Clue findById(String id);

    @Select({
            "select car.id as carId,u.name as username,a.* from tbl_activity a " ,
                    "left join tbl_user u on a.owner = u.id " ,
                    "left join tbl_clue_activity_relation car on a.id = car.activityId " ,
                    "where a.isDelete = '0' and car.clueId = #{clueId}"
    })
    List<Activity> findActivityRelationList(String clueId);

    @Delete("delete from tbl_clue_activity_relation where id = #{carId}")
    int deleteClueActivityRelation(String carId);

    @Select({
            "select car.id as carId,u.name as username,a.* from tbl_activity a " ,
            "left join tbl_user u on a.owner = u.id " ,
            "left join tbl_clue_activity_relation car on a.id != car.activityId " ,
            "where a.isDelete = '0' and car.clueId = #{clueId}"
    })
    List<Activity> findClueActivityUnRelationList(String clueId);

    List<Activity> findClueActivityUnRelationListLike(@Param("clueId") String clueId, @Param("activityName") String activityName);
}
