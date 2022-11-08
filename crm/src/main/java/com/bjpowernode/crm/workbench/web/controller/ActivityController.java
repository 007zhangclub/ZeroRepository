package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.Page;
import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/workbench/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /*
    跳转市场活动首页面
     */
    @RequestMapping("/toIndex.do")
    public String toIndex() {
        return "/workbench/activity/index";
    }


    @RequestMapping("/getActivityListPage.do")
    @ResponseBody
    public R getActivityListPage(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                    @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                    @RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "owner", required = false) String owner,
                                    @RequestParam(value = "startDate", required = false) String startDate,
                                    @RequestParam(value = "endDate", required = false) String endDate) {

        //根据分页条件来进行查询数据
        /*
            pageNo      分页参数
            pageSize    分页参数
            name        模糊查询
            owner       等值查询
            startDate   大于等于
            endDate     小于等于
         */
        List<Activity> activityList = activityService.findPage(
                pageNo,
                pageSize,
                name,
                owner,
                startDate,
                endDate
        );

        /*
            查询总记录数和计算总页数
         */
        int totalCounts = activityService.findPageCount(
                name,
                owner,
                startDate,
                endDate
        );

        int totalPages = totalCounts % pageSize == 0 ? totalCounts / pageSize : (totalCounts / pageSize) + 1 ;

        return new Page()
                .setRecords(activityList)
                .setTotalCounts(totalCounts)
                .setTotalPages(totalPages)
                .setPageNo(pageNo)
                .setPageSize(pageSize);
                //.setCode(State.SUCCESS.getCode())
                //.setMsg(State.SUCCESS.getMsg())
                //.setSuccess(true);
    }
}
