package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.workbench.dao.ActivityDao;
import com.bjpowernode.crm.workbench.dao.ActivityRemarkDao;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private ActivityRemarkDao activityRemarkDao;

    @Override
    public List<Activity> findPage(Integer pageNo, Integer pageSize, String name, String owner, String startDate, String endDate) {
        int pageNoIndex = (pageNo -1) * pageSize;
        return activityDao.findPage(pageNoIndex,pageSize,name,owner,startDate,endDate);
    }

    @Override
    public int findPageCount(String name, String owner, String startDate, String endDate) {
        return activityDao.findPageCount(name,owner,startDate,endDate);
    }

    @Override
    public boolean saveActivity(Activity activity) {
        return activityDao.insert(activity) > 0;
    }

    @Override
    public Activity findActivity(String id) {
        return activityDao.findById(id);
    }

    @Override
    public boolean updateActivity(Activity activity) {
        return activityDao.update(activity) > 0;
    }

    @Override
    public boolean updateIsDelete(List<Activity> activityList) {
        //for循环删除
        activityList.forEach(
                activity -> {
                    int count = activityDao.updateIsDelete(activity);

                    if(count<=0)
                        throw new RuntimeException(State.DB_DELETE_ERROR.getMsg());
                }
        );
        return true;
    }

    @Override
    public boolean saveActivityList(List<Activity> activityList) {
        return activityDao.insertList(activityList) > 0;
    }

    @Override
    public List<Activity> findActivityList(List<String> ids) {
        return activityDao.findAll(ids);
    }

    @Override
    public List<ActivityRemark> findActivityRemarkList(String activityId) {
        return activityRemarkDao.findList(activityId);
    }

    @Override
    public boolean saveActivityRemark(ActivityRemark activityRemark) {
        return activityRemarkDao.insert(activityRemark) > 0;
    }

    @Override
    public boolean updateActivityRemark(ActivityRemark activityRemark) {
        return activityRemarkDao.update(activityRemark) > 0;
    }

    @Override
    public boolean deleteActivityRemark(String remarkId) {
        return activityRemarkDao.delete(remarkId) > 0;
    }
}
