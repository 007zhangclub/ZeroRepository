package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.workbench.dao.ActivityDao;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    private ActivityDao activityDao;

    @Override
    public List<Activity> findPage(Integer pageNo, Integer pageSize, String name, String owner, String startDate, String endDate) {
        int pageNoIndex = (pageNo -1) * pageSize;
        return activityDao.findPage(pageNoIndex,pageSize,name,owner,startDate,endDate);
    }
}
