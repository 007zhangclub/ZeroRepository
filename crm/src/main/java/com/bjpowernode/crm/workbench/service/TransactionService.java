package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.Tran;

public interface TransactionService {
    boolean saveTransaction(Tran tran, String customerName, String name, String time);
}
