package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.Page;
import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/workbench/activity")
public class ActivityController extends Workbench {

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


    @RequestMapping("/saveActivity.do")
    @ResponseBody
    public R saveActivity(Activity activity, HttpSession session){
        /*
            我们在系统设置模块中,抽取了一个父类Settings
                我们将一些校验的方法,放到了这个父类中,进行调用,这样很方便
            现在我们写到了工作台模块,那么又需要用到这些方法,我们怎么办呢?
                1. 创建一个父类,将代码复制过来
                2. 将当前类继承自Settings
            *** 3. 创建一个公共的基类,再创建一个Workbench和Settings共同实现这个父类
                    将公共的代码放到基类中
         */
        checked(
                activity.getOwner(),
                activity.getName()
        );


        //String name = ((User) session.getAttribute("user")).getName();
        //String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //封装实体类的id,createBy,createTime,editBy,editTime这些属性
        //新增的市场活动默认是未删除的数据
        activity.setId(getId())
                .setCreateBy(getName())
                .setCreateTime(getTime())
                .setEditBy(getName())
                .setEditTime(getTime())
                .setIsDelete("0");

        //新增操作
        boolean flag = activityService.saveActivity(activity);

        return ok(flag);
    }


    @RequestMapping("/getActivity.do")
    @ResponseBody
    public R getActivity(@RequestParam("id")String id){
        return ok(
                activityService.findActivity(id)
        );
    }
}
