package com.bjpowernode.crm.settings.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/settings/user")
public class UserController {

    @Autowired
    private UserService userService;

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


    /*
    登录操作
     */
    @RequestMapping("/login.do")
    @ResponseBody//当前返回值转换为json数据返回
    //通过get或post的表单当时来传递数据
    public R login(@RequestParam("loginAct")String loginAct,
                   @RequestParam("loginPwd")String loginPwd,
                   HttpServletRequest request){
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
        User user = userService.findUserByLoginAct(loginAct,loginPwd);

        //登录成功,我们将User对象存入到Session中
        request.getSession().setAttribute("user",user);

        //登录成功,返回R对象
        return R.builder()
                .code(20000)
                .msg("请求成功")
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
