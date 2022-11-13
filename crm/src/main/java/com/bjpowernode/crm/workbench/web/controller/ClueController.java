package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.utils.IdUtils;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Clue;
import com.bjpowernode.crm.workbench.service.ClueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
