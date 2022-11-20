package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.dao.CustomerDao;
import com.bjpowernode.crm.workbench.dao.TranDao;
import com.bjpowernode.crm.workbench.dao.TranHistoryDao;
import com.bjpowernode.crm.workbench.domain.Customer;
import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;
import com.bjpowernode.crm.workbench.service.TransactionService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TranDao tranDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private TranHistoryDao tranHistoryDao;

    @Override
    public boolean saveTransaction(Tran tran, String customerName, String name, String time) {

        //根据客户名称,查询当前客户信息
        Customer customer = customerDao.findByName(customerName);

        if (ObjectUtils.isEmpty(customer)) {
            //新增客户信息
            customer = Customer.builder()
                    .id(IdUtils.getId())
                    .owner(tran.getOwner())
                    .name(customerName)
                    //.website()
                    //.phone()
                    .createBy(name)
                    .createTime(time)
                    .editBy(name)
                    .editTime(time)
                    .contactSummary(tran.getContactSummary())
                    .nextContactTime(tran.getNextContactTime())
                    .description(tran.getDescription())
                    //.address()
                    .build();

            customerDao.insert(customer);
        }

        //给交易赋值
        tran.setCustomerId(customer.getId())
                .setId(IdUtils.getId())
                .setCreateBy(name)
                .setCreateTime(time)
                .setEditBy(name)
                .setEditTime(time);

        //新增交易
        int a = tranDao.insert(tran);

        if (a <= 0)
            throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

        //新增交易历史记录
        TranHistory history = TranHistory.builder()
                .id(IdUtils.getId())
                .stage(tran.getStage())
                .money(tran.getMoney())
                .expectedDate(tran.getExpectedDate())
                .createTime(time)
                .createBy(name)
                .tranId(tran.getId())
                .build();

        tranHistoryDao.insert(history);

        return a > 0;
    }

    @Override
    public Tran findTransaction(String id) {
        return tranDao.findById(id);
    }

    @Override
    public List<TranHistory> findTransactionHistoryList(String tranId) {
        return tranHistoryDao.findList(tranId);
    }

    @Override
    public void updateStage(String tranId, String money, String expectedDate, String name, String time, String stage) {
        //更新交易阶段
        int a = tranDao.updateStage(
                tranId,
                stage,
                name,
                time
        );

        if (a <= 0)
            throw new RuntimeException(State.DB_UPDATE_ERROR.getMsg());

        //新增交易历史记录
        tranHistoryDao.insert(
                TranHistory.builder()
                        .id(IdUtils.getId())
                        .stage(stage)
                        .money(money)
                        .expectedDate(expectedDate)
                        .createTime(time)
                        .createBy(name)
                        .tranId(tranId)
                        .build()
        );
    }
}
