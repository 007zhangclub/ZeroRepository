package com.bjpowernode.crm.interceptor;

import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.exception.TraditionRequestException;
import com.bjpowernode.crm.settings.domain.User;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LoginInterceptor implements HandlerInterceptor {
    /*
        控制器执行前拦截的方法
            权限校验
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        /*
            返回false代表拦截,不再向控制器进行请求
            返回true代表放行,请求可以到达控制器中
         */
        //只有登录后,Session中包含User对象的请求,允许访问
        User user = (User) httpServletRequest.getSession().getAttribute("user");

        //没有登录,不允许访问
        if(ObjectUtils.isEmpty(user))
            //当前的请求不包含,登录和跳转到登录页面操作
            //return false;
            //也可以通过抛出异常的方式来控制页面的跳转
            throw new TraditionRequestException(State.USER_NO_AUTHORIZATION.getMsg());

        //放行
        return true;
    }

    /*
        控制器执行后拦截的方法
     */
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    /*
        页面加载前拦截的方法
     */
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
