package com.bjpowernode.crm.exception;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//通知类
@Slf4j
@ControllerAdvice
public class CustomHandlerException {

    /*
        当抛出RuntimeException异常时,我们捕获并返回自定义json数据
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public R getRuntimeException(Exception e){
        e.printStackTrace();
        String message = e.getMessage();
        //根据msg信息获取code码
        Integer code = State.getCodeByMsg(message);

        //组装公共的返回值信息,返回数据
        return R.builder()
                .code(code)
                .msg(message)
                .success(false)
                .build();
    }

    /*
    用户未登录,重定向跳转到登录页面
     */
    @ExceptionHandler(TraditionRequestException.class)
    public String getTraditionRequestException(Exception e){
        e.printStackTrace();

        //获取code和msg记录日志信息
        String message = e.getMessage();
        Integer code = State.getCodeByMsg(message);
        log.info("ERROR_CODE : {}, ERROR_MSG : {}",code,message);

        //没有登录,我们跳转到登录页面
        return "redirect:/settings/user/toLogin.do";
    }

}
