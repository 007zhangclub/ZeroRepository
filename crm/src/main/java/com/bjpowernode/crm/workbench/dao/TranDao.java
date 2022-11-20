package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Tran;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface TranDao {

    int insert(Tran tran);

    Tran findById(String id);

    @Update("update tbl_tran set stage=#{stage},editBy=#{name},editTime=#{time} where id = #{tranId}")
    int updateStage(@Param("tranId") String tranId, @Param("stage") String stage, @Param("name") String name, @Param("time") String time);
}
