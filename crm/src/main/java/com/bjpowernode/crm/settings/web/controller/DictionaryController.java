package com.bjpowernode.crm.settings.web.controller;

import com.bjpowernode.crm.settings.domain.DictionaryType;
import com.bjpowernode.crm.settings.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/settings/dictionary")
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    /*
    跳转到字典模块首页面
     */
    @RequestMapping("/toIndex.do")
    public String toIndex(){
        return "/settings/dictionary/index";
    }

    /*
    跳转到字典类型模块首页面
     */
    @RequestMapping("/type/toIndex.do")
    public String toTypeIndex(Model model){

        //查询出字典类型列表数据
        List<DictionaryType> dictionaryTypeList = dictionaryService.findDictionaryTypeList();

        //校验
        if(!CollectionUtils.isEmpty(dictionaryTypeList))
            //存入到model中,携带到页面
            model.addAttribute("dictionaryTypeList",dictionaryTypeList);

        return "/settings/dictionary/type/index";
    }

    /*
    跳转到字典值模块首页面
     */
    @RequestMapping("/value/toIndex.do")
    public String toValueIndex(){
        return "/settings/dictionary/value/index";
    }
}
