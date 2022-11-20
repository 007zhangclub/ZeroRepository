package com.bjpowernode.crm.workbench.base;

import com.bjpowernode.crm.base.Base;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.service.ActivityService;
import com.bjpowernode.crm.workbench.service.CustomerService;
import com.bjpowernode.crm.workbench.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

public class Workbench extends Base {

    @Autowired
    protected ActivityService activityService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected CustomerService customerService;

    @Autowired
    protected TransactionService transactionService;
}
