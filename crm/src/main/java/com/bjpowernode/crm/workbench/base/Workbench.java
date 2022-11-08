package com.bjpowernode.crm.workbench.base;

import com.bjpowernode.crm.base.Base;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;

public class Workbench extends Base {

    @Autowired
    protected ActivityService activityService;

}
