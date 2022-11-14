package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.Clue;
import com.bjpowernode.crm.workbench.domain.ClueRemark;
import com.bjpowernode.crm.workbench.service.ClueService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/workbench/clue")
public class ClueController extends Workbench {

    @Autowired
    private ClueService clueService;

    @RequestMapping("/toIndex.do")
    public String toIndex(){
        return "/workbench/clue/index";
    }


    @RequestMapping("/saveClue.do")
    @ResponseBody
    public R saveClue(@RequestBody Clue clue){
        //校验必传的参数信息
        checked(
                clue.getOwner(),
                clue.getFullname(),
                clue.getCompany()
        );

        //赋值操作
        clue.setId(IdUtils.getId())
                .setCreateBy(getName())
                .setCreateTime(getTime())
                .setEditBy(getName())
                .setEditTime(getTime());

        //新增操作
        return ok(
                clueService.saveClue(clue),
                State.DB_SAVE_ERROR
        );
    }


    @RequestMapping("/toDetail.do")
    public String toDetail(@RequestParam("id") String id, Model model){
        //根据id查询数据
        Clue clue = clueService.findClue(id);

        if(ObjectUtils.isNotEmpty(clue))
            model.addAttribute("clue",clue);

        return "/workbench/clue/detail";
    }



    @RequestMapping("/getClueRemarkList.do")
    @ResponseBody
    public R getClueRemarkList(@RequestParam String clueId){
        //根据线索id,查询备注信息列表数据
        List<ClueRemark> clueRemarkList = clueService.findClueRemarkList(clueId);

        return ok(clueRemarkList);
    }



    @RequestMapping("/remark/saveClueRemark.do")
    @ResponseBody
    public R saveClueRemark(@RequestBody ClueRemark clueRemark){
        //校验
        checked(
                clueRemark.getNoteContent(),
                clueRemark.getClueId()
        );

        //赋值操作
        clueRemark.setId(IdUtils.getId())
                .setEditFlag("0")
                .setEditTime(getTime())
                .setEditBy(getName())
                .setCreateTime(getTime())
                .setCreateBy(getName());

        //新增操作
        return ok(
                clueService.saveClueRemark(clueRemark),
                State.DB_SAVE_ERROR
        );
    }


    @RequestMapping("/getClueActivityRelationList.do")
    @ResponseBody
    public R getClueActivityRelationList(@RequestParam("clueId")String clueId){
        //根据线索id查询当前线索已关联的市场活动列表数据
        List<Activity> activityList = clueService.findClueActivityRelationList(clueId);

        return ok(activityList);
    }



    @RequestMapping("/deleteClueActivityRelation.do")
    @ResponseBody
    public R deleteClueActivityRelation(@RequestParam("carId")String carId){
        //根据carId来删除中间表的关联关系
        boolean flag = clueService.deleteClueActivityRelation(carId);

        return ok(flag,State.DB_DELETE_ERROR);
    }



    @RequestMapping("/getClueActivityUnRelationList.do")
    @ResponseBody
    public R getClueActivityUnRelationList(@RequestParam("clueId")String clueId,
                                           @RequestParam(value = "activityName",required = false)String activityName){

        //根据线索id查询未关联的市场活动列表数据
        List<Activity> activityList = clueService.findClueActivityUnRelationList(clueId);

        if(CollectionUtils.isEmpty(activityList)){
            //如果查询出的结果为空,证明所有的市场活动列表数据都可以进行关联,我们返回所有的市场活动列表数据
            activityList = activityService.findAll();

        }else{
            //如果查询出的结果不为空,并且市场活动名称不为空我们去模糊查询
            if(StringUtils.isNotBlank(activityName))
                //模糊查询
                activityList = clueService.findClueActivityUnRelationList(clueId,activityName);
        }

        return ok(activityList);
    }
}
