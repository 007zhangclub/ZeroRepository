package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.constants.ActivityConstants;
import com.bjpowernode.crm.entity.Page;
import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        String url = ActivityConstants.UPLOAD_URL+"Activity-"+getTime()+".xls";
        //文件上传操作
        activityFile.transferTo(
                //指定上传的路径及文件名称
                new File(url)
        );
        //---文件上传---

        List<Activity> activityList = new ArrayList<>();

        //---批量导入---
        //将Excel文件加载成输入流对象
        InputStream in = new FileInputStream(url);

        //基于输入流对象,创建工作簿对象
        Workbook workbook = new HSSFWorkbook(in);

        //通过工作簿对象,获取页码对象
        Sheet sheet = workbook.getSheetAt(0);

        //通过页码对象,获取最后的行号
        int lastRowNum = sheet.getLastRowNum();

        //遍历每一行的数据,必须要跳过第一行,因为第一行是表头数据
        for(int i=0; i<lastRowNum; i++){
            //根据页码对象,获取每一行的对象
            Row row = sheet.getRow(i + 1);

            //获取行中单元格的数据
            String activityName = row.getCell(0).getStringCellValue();
            String startDate = row.getCell(1).getStringCellValue();
            String endDate = row.getCell(2).getStringCellValue();
            String cost = row.getCell(3).getStringCellValue();
            String description = row.getCell(4).getStringCellValue();

            //封装到集合中
            activityList.add(
                    //封装市场活动对象
                    Activity.builder()
                            .id(IdUtils.getId())
                            .owner(getOwner())
                            .name(activityName)
                            .startDate(startDate)
                            .endDate(endDate)
                            .cost(cost)
                            .description(description)
                            .createTime(getTime())
                            .createBy(getName())
                            .editTime(getTime())
                            .editBy(getName())
                            .isDelete("0")
                            .build()
            );
        }

        //完成批量导入的操作
        boolean flag = activityService.saveActivityList(activityList);

        if(!flag)
            throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

        //---批量导入---
        return "redirect:/workbench/activity/toIndex.do";
    }



    /*
        批量导出操作
            由于我们的文件需要通过response对象进行相应回浏览器的操作
            我们这里不需要返回值void
     */
    @RequestMapping("/exportActivity.do")
    public void exportActivity(@RequestParam(value = "ids",required = false)List<String> ids,
                               HttpServletResponse response) throws IOException {
        //查询数据库的所有未删除的市场活动列表数据
        List<Activity> activityList = activityService.findActivityList(ids);

        if(CollectionUtils.isEmpty(activityList))
            //如果查询出的数据为空,那么也无需下载了
            return;

        //遍历数据,封装到Workbook对象中
        Workbook workbook  = new HSSFWorkbook();

        //基于工作簿对象,创建页码对象
        Sheet sheet = workbook.createSheet("市场活动列表");

        //基于页码对象,创建行对象,第一行是表头数据
        Row r0 = sheet.createRow(0);

        /*
        id
        owner
        name
        startDate
        endDate
        cost
        description
        createTime
        createBy
        editTime
        editBy
        isDelete
        username
         */
        r0.createCell(0).setCellValue("唯一标识");
        r0.createCell(1).setCellValue("用户标识");
        r0.createCell(2).setCellValue("用户名称");
        r0.createCell(3).setCellValue("活动名称");
        r0.createCell(4).setCellValue("开始时间");
        r0.createCell(5).setCellValue("结束时间");
        r0.createCell(6).setCellValue("成本");
        r0.createCell(7).setCellValue("描述");
        r0.createCell(8).setCellValue("创建时间");
        r0.createCell(9).setCellValue("创建人");
        r0.createCell(10).setCellValue("修改时间");
        r0.createCell(11).setCellValue("修改人");
        r0.createCell(12).setCellValue("状态");

        //遍历集合数据,封装数据到行和单元格对象中
        for (int i = 0; i < activityList.size(); i++) {
            Activity activity = activityList.get(i);

            //创建每一行的对象,封装单元格数据
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(activity.getId());
            row.createCell(1).setCellValue(activity.getOwner());
            row.createCell(2).setCellValue(activity.getUsername());
            row.createCell(3).setCellValue(activity.getName());
            row.createCell(4).setCellValue(activity.getStartDate());
            row.createCell(5).setCellValue(activity.getEndDate());
            row.createCell(6).setCellValue(activity.getCost());
            row.createCell(7).setCellValue(activity.getDescription());
            row.createCell(8).setCellValue(activity.getCreateTime());
            row.createCell(9).setCellValue(activity.getCreateBy());
            row.createCell(10).setCellValue(activity.getEditTime());
            row.createCell(11).setCellValue(activity.getEditBy());
            row.createCell(12).setCellValue(activity.getIsDelete().equals("0")?"正常":"");
        }

        //设置下载的文件名称
        response.addHeader(
                FileUploadBase.CONTENT_DISPOSITION,
                FileUploadBase.ATTACHMENT+";filename=Activity-"+getTime()+".xls"
        );

        //下载操作
        workbook.write(
                //通过response对象,获取输出流对象
                response.getOutputStream()
        );
    }


    @RequestMapping("/toDetail.do")
    public String toDetail(@RequestParam("id") String id, Model model){
        //查询市场活动数据
        Activity activity = activityService.findActivity(id);

        if(ObjectUtils.isNotEmpty(activity))
            model.addAttribute("activity",activity);

        return "/workbench/activity/detail";
    }


    @RequestMapping("/getActivityRemarkList.do")
    @ResponseBody
    public R getActivityRemarkList(@RequestParam("activityId") String activityId){
        //查询当前市场活动id相关联的列表数据
        List<ActivityRemark> activityRemarkList = activityService.findActivityRemarkList(activityId);

        return ok(activityRemarkList);
    }


    @RequestMapping("/remark/saveActivityRemark.do")
    @ResponseBody
    public R saveActivityRemark(@RequestBody ActivityRemark activityRemark){
        //赋值操作
        activityRemark.setId(IdUtils.getId())
                .setEditFlag("0")
                .setCreateTime(getTime())
                .setCreateBy(getName())
                .setEditTime(getTime())
                .setEditBy(getName());

        //新增操作
        boolean flag = activityService.saveActivityRemark(activityRemark);

        return ok(flag,State.DB_SAVE_ERROR);
    }


    @RequestMapping("/remark/updateActivityRemark.do")
    @ResponseBody
    public R updateActivityRemark(@RequestBody ActivityRemark activityRemark){
        //赋值操作
        activityRemark.setEditFlag("1")
                .setEditBy(getName())
                .setEditTime(getTime());

        //修改操作
        boolean flag = activityService.updateActivityRemark(activityRemark);

        return ok(flag,State.DB_UPDATE_ERROR);
    }


    @RequestMapping("/remark/deleteActivityRemark.do")
    @ResponseBody
    public R deleteActivityRemark(@RequestParam("remarkId") String remarkId){
        return ok(
                activityService.deleteActivityRemark(remarkId),
                State.DB_DELETE_ERROR
        );
    }


    @RequestMapping("/getActivityList.do")
    @ResponseBody
    public R<List<Activity>> getActivityList(@RequestParam(value = "activityName",required = false)String activityName){
        return ok(
                activityService.findAll(activityName)
        );
    }
}
