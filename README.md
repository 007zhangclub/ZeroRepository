# Crm项目笔记

## 异常处理器
* 异常处理器
```java
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
```

## 拦截器
* 拦截器
```java
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
```


## 十天免登录
* 前端代码
```javascript
function login() {
    //给登录按钮,添加点击事件
    $("#loginBtn").click(function () {
        //获取用户名和密码
        let loginAct = $("#loginAct").val();
        let loginPwd = $("#loginPwd").val();

        //校验用户名和密码
        if(loginAct == ""){
            //通过弹出框对错误信息进行提示
            alert("用户名不能为空")
            //return关键字的作用,是如果用户名为空,我们跳出方法,不再向下执行
            return;
        }

        if(loginPwd == ""){
            //在页面中准备标签进行展示提示信息
            $("#msg").html("密码不能为空");
            return;
        }

        //校验通过,清空提示信息
        $("#msg").html("");

        //发送post请求,进行登录操作
        post4m(
            $("#autoFlag").prop("checked") ? "settings/user/login.do?flag=a" : "settings/user/login.do",
            {
                loginAct:loginAct,
                loginPwd:loginPwd,
                //flag: $("#autoFlag").prop("checked") ? "a" : ""
            },data=>{
                //data是我们服务器返回的数据
                // {code:20000,msg:xxx,success:true} 请求成功的返回信息
                // {code:20001,msg:xxx,success:false} 请求失败的返回信息
                //如果是查询 {code:20000,msg:xxx,success:true,data:xxx}
                if(data.success)
                    //登录成功,后续我们要跳转到工作台首页面
                    //$("#msg").html(data.msg);
                    to("workbench/toIndex.do")
                else
                    $("#msg").html(data.msg);
            }
        )
    })
}
```

* 后台代码 `controller`
```java
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
```

* 后台代码 `service`
```java
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
```