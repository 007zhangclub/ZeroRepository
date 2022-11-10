package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.constants.ActivityConstants;
import com.bjpowernode.crm.entity.Page;
import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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



    @RequestMapping("/updateActivity.do")
    @ResponseBody
    public R updateActivity(@RequestBody Activity activity){
        //修改之前,先行赋值
        activity.setEditTime(getTime())
                .setEditBy(getName());

        return ok(
                //修改操作
                activityService.updateActivity(activity),
                State.DB_UPDATE_ERROR
        );
    }


    @RequestMapping("/deleteActivityList.do")
    @ResponseBody
    public R deleteActivityList(@RequestParam("ids") List<String> ids){
        checked(ids);

        //修改isDelete属性为1,代表已删除,同时更新修改人和修改时间
        //批量更新操作
        //update tbl_activity set isDelete = 1 where id in (?,?,?)
        //update tbl_activity set isDelete = 1, editBy = ? , editTime = ? where id in (?,?,?)
        //通过遍历,将ids转换为activityList
        List<Activity> activityList = ids.stream()
                .map(id -> new Activity().setId(id).setIsDelete("1").setEditBy(getName()).setEditTime(getTime()))
                .collect(Collectors.toList());

        //批量更新
        return ok(
                activityService.updateIsDelete(activityList),
                State.DB_DELETE_ERROR
        );
    }



    /*
        批量导入操作 = 文件上传 + 批量新增
     */
    @RequestMapping("/uploadActivityFile.do")
    public String uploadActivityFile(@RequestParam("activityFile")MultipartFile activityFile) throws IOException {
        //---文件上传---
        //获取文件名称, aa.xls
        String originalFilename = activityFile.getOriginalFilename();

        //获取文件后缀名
        String suffix = originalFilename.substring(
                originalFilename.lastIndexOf(".") + 1
        );

        //校验文件上传的是否是Excel文件
        if(!StringUtils.equalsAny(suffix,"xls","xlsx"))
            throw new RuntimeException(State.UPLOAD_FILE_ERROR.getMsg());

        //校验文件上传的大小,文件小于5MB
        long size = activityFile.getSize() / 1000 / 1000;
        if(size > 5)
            throw new RuntimeException(State.UPLOAD_FILE_SIZE_ERROR.getMsg());

        //判断上传的路径是否存在,如果不存在,则需要创建
        if(!new File(ActivityConstants.UPLOAD_URL).exists())
            new File(ActivityConstants.UPLOAD_URL).mkdirs();

        //文件上传操作
        activityFile.transferTo(
                //指定上传的路径及文件名称
                new File(ActivityConstants.UPLOAD_URL+"Activity-"+getTime()+".xls")
        );
        //---文件上传---

        //---批量导入---
        //---批量导入---
        return "redirect:/workbench/activity/toIndex.do";
    }
}
