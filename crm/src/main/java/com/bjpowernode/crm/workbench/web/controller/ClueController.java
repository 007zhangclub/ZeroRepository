package com.bjpowernode.crm.workbench.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/workbench/clue")
public class ClueController {

    @RequestMapping("/toIndex.do")
    public String toIndex(){
        return "/workbench/clue/index";
    }
}
