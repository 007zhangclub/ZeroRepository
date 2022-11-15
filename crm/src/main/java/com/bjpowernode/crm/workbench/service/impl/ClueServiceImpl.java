package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.workbench.dao.ClueDao;
import com.bjpowernode.crm.workbench.dao.ClueRemarkDao;
import com.bjpowernode.crm.workbench.domain.*;
import com.bjpowernode.crm.workbench.service.ClueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClueServiceImpl implements ClueService {
    @Autowired
    private ClueDao clueDao;

    @Autowired
    private ClueRemarkDao clueRemarkDao;

    @Override
    public boolean saveClue(Clue clue) {
        return clueDao.insert(clue) > 0;
    }

    @Override
    public Clue findClue(String id) {
        return clueDao.findById(id);
    }

    @Override
    public List<ClueRemark> findClueRemarkList(String clueId) {
        return clueRemarkDao.findList(clueId);
    }

    @Override
    public boolean saveClueRemark(ClueRemark clueRemark) {
        return clueRemarkDao.insert(clueRemark) > 0;
    }

    @Override
    public List<Activity> findClueActivityRelationList(String clueId) {
        return clueDao.findActivityRelationList(clueId);
    }

    @Override
    public boolean deleteClueActivityRelation(String carId) {
        return clueDao.deleteClueActivityRelation(carId) > 0;
    }

    @Override
    public List<Activity> findClueActivityUnRelationList(String clueId) {
        return clueDao.findClueActivityUnRelationList(clueId);
    }

    @Override
    public List<Activity> findClueActivityUnRelationList(String clueId, String activityName) {
        return clueDao.findClueActivityUnRelationListLike(clueId,activityName);
    }

    @Override
    public boolean saveClueActivityRelationList(List<ClueActivityRelation> clueActivityRelationList) {
        return clueDao.insertClueActivityRelationList(clueActivityRelationList) > 0;
    }

    @Override
    public List<Activity> findClueActivityRelationList(String clueId, String activityName) {
        return clueDao.findActivityRelationListLike(clueId,activityName);
    }


    /*
        线索转换操作业务逻辑梳理:
            1. 根据线索id,查询线索数据
            2. 获取线索数据中的客户名称(company)和线索中的联系人名称(fullname)
            3. 根据客户名称查询是否有当前客户信息
                如果查询出的结果是null,新增客户信息
                如果有当前的客户信息,直接使用即可
            4. 根据联系人名称查询是否有当前联系人信息
                如果查询出的结果是null,新增联系人信息
                如果有当前的联系人信息,直接使用即可
            -----------------一对一的线索转换操作-----------------
            5. 根据线索id,查询线索关联的备注信息列表数据
                如果线索备注信息列表不为null,我们需要将线索备注信息列表转换为客户备注信息列表和联系人的备注信息列表
            -----------------一对多的线索转换操作-----------------
            6. 根据线索id,查询线索关联的中间表数据(线索和市场活动的中间表数据)
                如果线索关联的中间表数据不为null,我们需要将线索和市场活动的中间表数据,转换为联系人和市场活动的中间表数据
            -----------------多对多的线索转换操作-----------------
            7. 是否需要新增交易记录
                根据flag或者tran是否为null,来判断是否需要新增交易记录
                    如果需要新增交易记录,那么我们则新增交易和交易历史记录
                如果无需创建交易,跳过此步骤即可
            8. 根据线索id,删除线索和市场活动的中间表记录
            9. 根据线索id,删除线索备注信息列表数据
            10. 根据线索id,删除线索数据

        表结构的梳理:
            线索的一对一转换
                tbl_clue -> tbl_customer 和 tbl_contacts
            线索的一对多转换
                tbl_clue_remark -> tbl_customer_remark 和 tbl_contacts_remark
            线索的多对多转换
                tbl_clue_activity_relation -> tbl_contacts_activity_relation
            数据已经转换到其他表中了,所以我们转换完成的最后就可以将线索和线索相关的表的数据删除掉
     */
    @Override
    public void saveClueConvert(String clueId, String flag, Tran tran, String owner, String name, String time) {

    }
}
