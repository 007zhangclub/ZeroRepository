package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.dao.*;
import com.bjpowernode.crm.workbench.domain.*;
import com.bjpowernode.crm.workbench.service.ClueService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClueServiceImpl implements ClueService {

    @Autowired
    private TranDao tranDao;

    @Autowired
    private TranHistoryDao tranHistoryDao;

    @Autowired
    private ClueDao clueDao;

    @Autowired
    private ClueRemarkDao clueRemarkDao;

    @Autowired
    private ContactsDao contactsDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerRemarkDao customerRemarkDao;

    @Autowired
    private ContactsRemarkDao contactsRemarkDao;

    @Autowired
    private ClueActivityRelationDao clueActivityRelationDao;

    @Autowired
    private ContactsActivityRelationDao contactsActivityRelationDao;

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
        return clueDao.findClueActivityUnRelationListLike(clueId, activityName);
    }

    @Override
    public boolean saveClueActivityRelationList(List<ClueActivityRelation> clueActivityRelationList) {
        return clueDao.insertClueActivityRelationList(clueActivityRelationList) > 0;
    }

    @Override
    public List<Activity> findClueActivityRelationList(String clueId, String activityName) {
        return clueDao.findActivityRelationListLike(clueId, activityName);
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

        //1. 根据线索id,查询线索数据
        Clue clue = clueDao.findById(clueId);

        //2. 获取线索数据中的客户名称(company)和线索中的联系人名称(fullname)
        String customerName = clue.getCompany();

        String contactsName = clue.getFullname();

        //3. 根据客户名称查询是否有当前客户信息
        //如果有当前的客户信息,直接使用即可
        Customer customer = customerDao.findByName(customerName);

        if (ObjectUtils.isEmpty(customer)) {
            //如果查询出的结果是null,新增客户信息
            customer = Customer.builder()
                    .id(IdUtils.getId())
                    .owner(owner)
                    .name(customerName)
                    .website(clue.getWebsite())
                    .phone(clue.getPhone())
                    .createBy(name)
                    .createTime(time)
                    .editBy(name)
                    .editTime(time)
                    .contactSummary(clue.getContactSummary())
                    .nextContactTime(clue.getNextContactTime())
                    .description(clue.getDescription())
                    .address(clue.getAddress())
                    .build();

            //新增客户
            int a = customerDao.insert(customer);

            if (a <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());
        }

        //4. 根据联系人名称查询是否有当前联系人信息
        //如果有当前的联系人信息,直接使用即可
        Contacts contacts = contactsDao.findByFullName(contactsName, clue.getMphone());

        if (ObjectUtils.isEmpty(contacts)) {
            //如果查询出的结果是null,新增联系人信息
            contacts = Contacts.builder()
                    .id(IdUtils.getId())
                    .owner(owner)
                    .source(clue.getSource())
                    .customerId(customer.getId())
                    .fullname(contactsName)
                    .appellation(clue.getAppellation())
                    .email(clue.getEmail())
                    .mphone(clue.getMphone())
                    .job(clue.getJob())
                    //.birth()
                    .createBy(name)
                    .createTime(time)
                    .editBy(name)
                    .editTime(time)
                    .description(clue.getDescription())
                    .contactSummary(clue.getContactSummary())
                    .nextContactTime(clue.getNextContactTime())
                    .address(clue.getAddress())
                    .build();

            int b = contactsDao.insert(contacts);

            if (b <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());
        }

        //-----------------一对一的线索转换操作-----------------

        //5. 根据线索id,查询线索关联的备注信息列表数据
        List<ClueRemark> clueRemarkList = clueRemarkDao.findList(clueId);

        String contactsId = contacts.getId();
        String customerId = customer.getId();

        //如果线索备注信息列表不为null,我们需要将线索备注信息列表转换为客户备注信息列表和联系人的备注信息列表
        if (!CollectionUtils.isEmpty(clueRemarkList)) {
            //这里也可以通过for循环来遍历转换
            //我这里演示的是使用stream api的方式来操作
            List<ContactsRemark> contactsRemarkList = clueRemarkList.stream().map(
                    remark -> ContactsRemark.builder()
                            .id(IdUtils.getId())
                            .noteContent(remark.getNoteContent())
                            .createBy(name)
                            .createTime(time)
                            .editBy(name)
                            .editTime(time)
                            .editFlag("1")
                            .contactsId(contactsId)
                            .build()
            ).collect(Collectors.toList());


            List<CustomerRemark> customerRemarkList = clueRemarkList.stream().map(
                    remark -> CustomerRemark.builder()
                            .id(IdUtils.getId())
                            .noteContent(remark.getNoteContent())
                            .createBy(name)
                            .createTime(time)
                            .editBy(name)
                            .editTime(time)
                            .editFlag("1")
                            .customerId(customerId)
                            .build()
            ).collect(Collectors.toList());

            //批量新增操作
            int c = contactsRemarkDao.insertList(contactsRemarkList);

            if (c <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

            int d = customerRemarkDao.insertList(customerRemarkList);

            if (d <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());
        }

        //-----------------一对多的线索转换操作-----------------

        //6. 根据线索id,查询线索关联的中间表数据(线索和市场活动的中间表数据)
        List<ClueActivityRelation> clueActivityRelationList = clueActivityRelationDao.findList(clueId);

        //如果线索关联的中间表数据不为null,我们需要将线索和市场活动的中间表数据,转换为联系人和市场活动的中间表数据
        if (!CollectionUtils.isEmpty(clueActivityRelationList)) {

            List<ContactsActivityRelation> carList = clueActivityRelationList.stream().map(
                    clueActivityRelation -> ContactsActivityRelation.builder()
                            .id(IdUtils.getId())
                            .contactsId(contactsId)
                            .activityId(clueActivityRelation.getActivityId())
                            .build()
            ).collect(Collectors.toList());

            int e = contactsActivityRelationDao.insertList(carList);

            if (e <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());
        }

        //-----------------多对多的线索转换操作-----------------

        //7. 是否需要新增交易记录
        //根据flag或者tran是否为null,来判断是否需要新增交易记录
        //如果无需创建交易,跳过此步骤即可

        if (StringUtils.equals(flag, "a")) {
            //如果需要新增交易记录,那么我们则新增交易和交易历史记录
            //交易是我们付款完成后的操作,交易历史记录是付款的凭证信息
            //name,money,expectedDate,stage,activityId
            tran.setId(IdUtils.getId())
                    .setOwner(owner)
                    .setContactsId(contactsId)
                    .setCustomerId(customerId)
                    //.setType()
                    .setSource(clue.getSource())
                    .setCreateBy(name)
                    .setCreateTime(time)
                    .setEditBy(name)
                    .setEditTime(time)
                    .setDescription(clue.getDescription())
                    .setContactSummary(clue.getContactSummary())
                    .setNextContactTime(clue.getNextContactTime());

            //新增交易
            int f = tranDao.insert(tran);

            if (f <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

            //新增交易历史记录
            int g = tranHistoryDao.insert(
                    TranHistory.builder()
                            .id(IdUtils.getId())
                            .stage(tran.getStage())
                            .money(tran.getMoney())
                            .expectedDate(clue.getAddress())
                            .createTime(time)
                            .createBy(name)
                            .tranId(tran.getId())
                            .build()
            );

            if (g <= 0)
                throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());
        }

        //8. 根据线索id,删除线索和市场活动的中间表记录
        int h = clueActivityRelationDao.deleteByClueId(clueId);

        if (h <= 0)
            throw new RuntimeException(State.DB_DELETE_ERROR.getMsg());

        //9. 根据线索id,删除线索备注信息列表数据
        int i = clueRemarkDao.deleteByClueId(clueId);

        if (i <= 0)
            throw new RuntimeException(State.DB_DELETE_ERROR.getMsg());

        //10. 根据线索id,删除线索数据
        int j = clueDao.delete(clueId);

        if (j <= 0)
            throw new RuntimeException(State.DB_DELETE_ERROR.getMsg());
    }
}
