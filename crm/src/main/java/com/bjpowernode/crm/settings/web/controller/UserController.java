package com.bjpowernode.crm.settings.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/settings/user")
public class UserController {

    /*
        跳转到登录页面
     */
    @RequestMapping("/toLogin.do")
    public String toLogin(){
        //视图解析器会根据返回的字符串进行拼接,找到我们指定的页面路径
        //前缀: /WEB-INF/jsp
        //返回值: /login
        //后缀: .jsp
        return "/login";
    }

}
