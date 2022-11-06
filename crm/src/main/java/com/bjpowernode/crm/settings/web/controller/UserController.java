package com.bjpowernode.crm.settings.web.controller;

import com.bjpowernode.crm.constants.UserConstants;
import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/settings/user")
public class UserController {

    @Autowired
    private UserService userService;

    /*
    跳转到登录页面 + 十天免登录② (自动登录,从Cookie中获取用户名和密码自动登录)
     */
    @RequestMapping(UserConstants.TO_LOGIN_URL)
    public String toLogin(HttpServletRequest request){

        /*
        十天免登录操作②,从Cookie中获取用户名和密码完成自动登录,跳转到工作台页面的功能
         */
        Cookie[] cookies = request.getCookies();

        //遍历Cookie
        String loginAct = "";
        String loginPwd = "";

//          通过普通for循环来遍历获取
//        if(ObjectUtils.isNotEmpty(cookies)){
//            for (Cookie cookie : cookies) {
//                if(cookie.getName().equals(UserConstants.LOGIN_ACT_PREFIX)){
//                    loginAct = cookie.getValue();
//                    continue;
//                }
//                if(cookie.getName().equals(UserConstants.LOGIN_PWD_PREFIX))
//                    loginPwd = cookie.getValue();
//            }
//        }

        if(ObjectUtils.isNotEmpty(cookies)){
            // 通过stream api的方式来遍历获取
            List<Cookie> cookieList = Arrays.asList(cookies);

            //通过filter方法来过滤出集合中的数据的name为loginAct的元素,将它转换为集合(只有一条数据)
            List<Cookie> loginActList = cookieList.stream().filter(cookie -> cookie.getName().equals(UserConstants.LOGIN_ACT_PREFIX)).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(loginActList))
                loginAct = loginActList.get(0).getValue();

            List<Cookie> loginPwdList = cookieList.stream().filter(cookie -> cookie.getName().equals(UserConstants.LOGIN_PWD_PREFIX)).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(loginPwdList))
                loginPwd = loginPwdList.get(0).getValue();
        }

        if(StringUtils.isNoneBlank(loginAct,loginPwd)){
            //获取ip地址
            String ip = request.getRemoteAddr();
            //自动登录操作
            User user = userService.findUserByLoginActAndLoginPwd(loginAct,loginPwd,ip);

            if(ObjectUtils.isNotEmpty(user)){
                //存入Session,并跳转到工作台页面
                request.getSession().setAttribute(UserConstants.USER_PREFIX,user);

                return "/workbench/index";
            }

        }


        //视图解析器会根据返回的字符串进行拼接,找到我们指定的页面路径
        //前缀: /WEB-INF/jsp
        //返回值: /login
        //后缀: .jsp
        return UserConstants.LOGIN_PAGE;
    }


    /*
    登录操作 + 十天免登录操作① (将用户名和密码(加密的)存入到Cookie中)
     */
    @RequestMapping(UserConstants.LOGIN_URL)
    @ResponseBody//当前返回值转换为json数据返回
    //通过get或post的表单当时来传递数据
    public R login(@RequestParam(UserConstants.LOGIN_ACT_PREFIX)String loginAct,
                   @RequestParam(UserConstants.LOGIN_PWD_PREFIX)String loginPwd,
                   @RequestParam(required = false) String flag,
                   HttpServletRequest request, HttpServletResponse response){
    //public R login(User user){
    //通过post的json数据传递
    //public R login(@RequestBody Map<String,String> user){
    //public R login(@RequestBody User user){
        //根据用户名和密码进行登录操作
        /*
            可以根据用户名和密码进行查询用户对象
                如果我们通过md5的方式对密码进行加密,那么可以选择这种
                因为md5加密出来的字符串是固定的
            也可以根据用户名查询对象,然后匹配密码
                如果我们通过bcrypt方式对密码进行加密,那么可以选择这种
                因为bcrypt加密出来的字符串是随机的,比如同样的密码获取出的加密字符串是不同的
                更安全
         */
        //获取ip地址
        //细节: 浏览器访问的时候不要使用localhost进行访问,因为它不是一个ip地址,使用127.0.0.1这个本地ip来访问
        String ip = request.getRemoteAddr();

        User user = userService.findUserByLoginAct(loginAct,loginPwd,ip);

        //登录成功,我们将User对象存入到Session中
        request.getSession().setAttribute(UserConstants.USER_PREFIX,user);

        //十天免登录①
        /*
        将用户名和已加密的密码存入到Cookie中,为后续自动登录提供服务
         */
        if(StringUtils.isNotBlank(flag)){

            if(!StringUtils.equals(flag,"a"))
                //传入标记异常
                throw new RuntimeException(State.PARAMS_ERROR.getMsg());

            //将用户名和密码存入到Cookie中,从user对象中获取
            Cookie loginActCookie = new Cookie(UserConstants.LOGIN_ACT_PREFIX,user.getLoginAct());
            Cookie loginPwdCookie = new Cookie(UserConstants.LOGIN_PWD_PREFIX,user.getLoginPwd());

            //设置Cookie的属性
            loginActCookie.setPath("/");
            loginActCookie.setMaxAge(60 * 60 * 24 * 10);
            loginPwdCookie.setPath("/");
            loginPwdCookie.setMaxAge(60 * 60 * 24 * 10);

            //通过response对象,将Cookie写回浏览器中
            response.addCookie(loginActCookie);
            response.addCookie(loginPwdCookie);
        }

        //登录成功,返回R对象
        return R.builder()
                .code(State.SUCCESS.getCode())
                .msg(State.SUCCESS.getMsg())
                .success(true)
                .build();
    }


    public static void main(String[] args) {
        /*
            $2a$10$YpIKNcC6aIfs52TeNvAJJ.QseHqP9hX45ZNTFovgtI9BIfZ5zG7Ye
            $2a$10$KB.WGXwc0udiOMG7a38Tm.c1HfMIsXfmuNN74JW0fJXVjQ2pafkN6
            $2a$10$sbJjAYtogpewINPG5q7eIeBCOIdXQM9lDttpS.QWWhdc/mqOLYqou
            $2a$10$DRUj37f1lFmj.juj1Ilyl.XIQKawYDlwwajgsXKhikLCgMW0BWO.6
            $2a$10$9Ib3Akf78vBbgvPrb8lt6O5V/vUiOkdxoTd.56JaDCXRaH9Y6vepq
            $2a$10$S3r9vHJI/MBSlNYckp9rX.99pIFOges3r1whXaNbSzXaH4uXuHpOO
            $2a$10$r2aqhhA986740Dr3IMwBZu2XqBpBDyFNK02LxMB8gs/abCruk9Gz2
            $2a$10$Ryx9Q9hOv7GXn.z9AqZwc.fu9FCCTIticM.RA6TfR3ucRoihulTjm
            $2a$10$.4Nfg8etYiCoHSMIGQE7AecAXLNCQwJJv8QqS6X6/XZ7i/.zL6xAq
            $2a$10$/yb1t7FafGEeykjCSAvva.FGrArZEIf3hyz1ymQLiC.griV82Jgs6
         */
        //普通for循环的遍历
        //for(int i=0; i<10; i++) System.out.println(new BCryptPasswordEncoder().encode("123"));

        //foreach遍历
        //List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        //integerList.forEach(i -> System.out.println(new BCryptPasswordEncoder().encode("123")));

        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$YpIKNcC6aIfs52TeNvAJJ.QseHqP9hX45ZNTFovgtI9BIfZ5zG7Ye"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$KB.WGXwc0udiOMG7a38Tm.c1HfMIsXfmuNN74JW0fJXVjQ2pafkN6"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$sbJjAYtogpewINPG5q7eIeBCOIdXQM9lDttpS.QWWhdc/mqOLYqou"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$DRUj37f1lFmj.juj1Ilyl.XIQKawYDlwwajgsXKhikLCgMW0BWO.6"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$9Ib3Akf78vBbgvPrb8lt6O5V/vUiOkdxoTd.56JaDCXRaH9Y6vepq"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$S3r9vHJI/MBSlNYckp9rX.99pIFOges3r1whXaNbSzXaH4uXuHpOO"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$r2aqhhA986740Dr3IMwBZu2XqBpBDyFNK02LxMB8gs/abCruk9Gz2"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$Ryx9Q9hOv7GXn.z9AqZwc.fu9FCCTIticM.RA6TfR3ucRoihulTjm"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$.4Nfg8etYiCoHSMIGQE7AecAXLNCQwJJv8QqS6X6/XZ7i/.zL6xAq"));
        System.out.println(new BCryptPasswordEncoder().matches("123","$2a$10$/yb1t7FafGEeykjCSAvva.FGrArZEIf3hyz1ymQLiC.griV82Jgs6"));

    }

}
