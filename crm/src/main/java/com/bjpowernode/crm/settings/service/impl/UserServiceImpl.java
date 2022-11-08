package com.bjpowernode.crm.settings.service.impl;

import com.bjpowernode.crm.constants.UserConstants;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.dao.UserDao;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;


    @Override
    public User findUserByLoginAct(String loginAct, String loginPwd, String ip) {

        //根据用户名查询用户对象
        User user = userDao.findUserByLoginAct(loginAct);

        //校验用户是否存在
        if(ObjectUtils.isEmpty(user))
            //抛出异常,代码不再向下进行
            throw new RuntimeException(State.USER_NOT_EXISTS.getMsg());

        //根据用户的密码进行比对
        //参数1,明文密码(前端传递过来的),参数2,密文密码(数据库查询出来的)
        boolean matches = new BCryptPasswordEncoder().matches(loginPwd, user.getLoginPwd());

        if(!matches)
            throw new RuntimeException(State.USER_LOGIN_ACT_OR_LOGIN_PWD_ERROR.getMsg());

        checkUser(user,ip);

        //用户登录成功
        return user;
    }

    private void checkUser(User user, String ip) {
        //登录属性校验
        /*
            过期时间,和当前时间进行比对,如果当前时间比过期时间大,证明账户已过期
            锁定状态,0代表未锁定,1代表已锁定
            ip受限列表,地址列表中包含的是允许访问的ip地址列表
         */
        //过期时间,和当前时间进行比对,如果当前时间比过期时间大,证明账户已过期
        String expireTime = user.getExpireTime();

        if (StringUtils.isNotBlank(expireTime)){
            //获取当前时间
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(UserConstants.DATE_TIME_19));

            //已过期
            if(expireTime.compareTo(now) <= 0)
                throw new RuntimeException(State.USER_ACCOUNT_EXPIRED_ERROR.getMsg());
        }

        //锁定状态,0代表未锁定,1代表已锁定
        String lockState = user.getLockState();

        if(StringUtils.isNotBlank(lockState))
            if(StringUtils.equals(lockState,UserConstants.LOCKED))
                throw new RuntimeException(State.USER_ACCOUNT_LOCKED_ERROR.getMsg());

        //ip受限列表,地址列表中包含的是允许访问的ip地址列表
        //127.0.0.1,192.168.1.1,....
        String allowIps = user.getAllowIps();

        if(StringUtils.isNotBlank(allowIps))
            if(!allowIps.contains(ip))
                throw new RuntimeException(State.USER_ACCOUNT_IP_NOT_ALLOW_ERROR.getMsg());
    }

    @Override
    public User findUserByLoginActAndLoginPwd(String loginAct, String loginPwd, String ip) {

        //根据用户名和密码查询用户信息
        User user = userDao.findUserByLoginActAndLoginPwd(loginAct,loginPwd);

        if(ObjectUtils.isEmpty(user))
            //抛出异常,代码不再向下进行
            throw new RuntimeException(State.USER_LOGIN_ACT_OR_LOGIN_PWD_ERROR.getMsg());

        checkUser(user,ip);

        return user;
    }

    @Override
    public List<User> findUserList() {
        return userDao.findAll();
    }

    public static void main(String[] args) throws ParseException {
        //compare方式适用于年月日的比对
//        String expireTime = "2022-11-11";
//        String endTime = "2022-11-11";
        String expireTime = "2022-11-11 12:12:13";
        String endTime = "2022-11-11 12:12:14";

        //如果a比b小返回值是-1
        //如果a比b大返回值1
        //如果a等于b返回值0
        System.out.println(expireTime.compareTo(endTime));

        //转换成Date对象进行比对
        Date expireTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(expireTime);
        Date endTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);

        System.out.println(expireTimeDate.before(endTimeDate));
        System.out.println(expireTimeDate.after(endTimeDate));
    }
}
