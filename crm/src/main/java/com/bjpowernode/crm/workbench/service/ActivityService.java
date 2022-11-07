package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.Activity;

import java.util.List;

public interface ActivityService {
    List<Activity> findPage(Integer pageNo, Integer pageSize, String name, String owner, String startDate, String endDate);
}
