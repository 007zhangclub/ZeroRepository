# Crm项目笔记

## 用户表介绍
* tbl_user
  * id `唯一标识`
  * loginAct `用户名`
  * name `昵称`
  * loginPwd `密码`
  * email `邮箱`
  * expireTime `过期时间`
  * lockState `锁定状态`
  * deptno
  * allowIps `ip允许访问列表`
  * createTime
  * createBy
  * editTime
  * editBy
## 基本登录操作
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
            "settings/user/login.do",
            {
                loginAct:loginAct,
                loginPwd:loginPwd,
            },data=>{
                //data是我们服务器返回的数据
                // {code:20000,msg:xxx,success:true} 请求成功的返回信息
                // {code:20001,msg:xxx,success:false} 请求失败的返回信息
                //如果是查询 {code:20000,msg:xxx,success:true,data:xxx}
                if(data.success)
                    alert(data.msg);
            }
        )
    })
}
```

* 后台代码 `controller`
```java
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
```

* 后台代码 `service`
```java
public User findUserByLoginAct(String loginAct, String loginPwd) {

    //根据用户名查询用户对象
    User user = userDao.findUserByLoginAct(loginAct);

    //校验用户是否存在
    if(ObjectUtils.isEmpty(user))
        //抛出异常,代码不再向下进行
        throw new RuntimeException("用户查询异常");

    //根据用户的密码进行比对
    //参数1,明文密码(前端传递过来的),参数2,密文密码(数据库查询出来的)
    boolean matches = new BCryptPasswordEncoder().matches(loginPwd, user.getLoginPwd());

    if(!matches)
        throw new RuntimeException("用户名或密码错误");

    //用户登录成功
    return user;
}
```

## 优化登录操作
* 后台代码 `controller`
```java
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
    //获取ip地址
    //细节: 浏览器访问的时候不要使用localhost进行访问,因为它不是一个ip地址,使用127.0.0.1这个本地ip来访问
    String ip = request.getRemoteAddr();

    User user = userService.findUserByLoginAct(loginAct,loginPwd,ip);

    //登录成功,我们将User对象存入到Session中
    request.getSession().setAttribute("user",user);

    //登录成功,返回R对象
    return R.builder()
            .code(20000)
            .msg("请求成功")
            .success(true)
            .build();
}
```

* 后台代码 `service`
```java
@Override
public User findUserByLoginAct(String loginAct, String loginPwd, String ip) {

    //根据用户名查询用户对象
    User user = userDao.findUserByLoginAct(loginAct);

    //校验用户是否存在
    if(ObjectUtils.isEmpty(user))
        //抛出异常,代码不再向下进行
        throw new RuntimeException("用户查询异常");

    //根据用户的密码进行比对
    //参数1,明文密码(前端传递过来的),参数2,密文密码(数据库查询出来的)
    boolean matches = new BCryptPasswordEncoder().matches(loginPwd, user.getLoginPwd());

    if(!matches)
        throw new RuntimeException("用户名或密码错误");

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
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //已过期
        if(expireTime.compareTo(now) <= 0)
            throw new RuntimeException("当前账号已过期,请联系管理人员");
    }

    //锁定状态,0代表未锁定,1代表已锁定
    String lockState = user.getLockState();

    if(StringUtils.isNotBlank(lockState))
        if(StringUtils.equals(lockState,"1"))
            throw new RuntimeException("当前账号已锁定,请联系管理人员");

    //ip受限列表,地址列表中包含的是允许访问的ip地址列表
    //127.0.0.1,192.168.1.1,....
    String allowIps = user.getAllowIps();

    if(StringUtils.isNotBlank(allowIps))
        if(!allowIps.contains(ip))
            throw new RuntimeException("当前账号IP地址受限,请联系管理人员");

    //用户登录成功
    return user;
}
```

## 权限校验
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
            return false;

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