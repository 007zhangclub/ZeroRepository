package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    boolean saveTransaction(Tran tran, String customerName, String name, String time);

    Tran findTransaction(String id);

    List<TranHistory> findTransactionHistoryList(String tranId);

    void updateStage(String tranId, String money, String expectedDate, String name, String time, String stage);

    List<Map<String, Object>> getChartDate();
}
