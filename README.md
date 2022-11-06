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


## 字典模块表结构
* tbl_dic_type `字典类型表` `一方`
    * code `主键,唯一标识,前台设置`
    * name
    * description
* tbl_dic_value `字典值表` `多方`
    * id `主键,唯一标识,后台设置`
    * text
    * value
    * orderNo
    * typeCode
```html
<select>性别
    <option value="male(value)">男(text)</option>
    <option>女</option>
</select>
```

## Crm模块介绍
* 系统设置模块 `settings`
  * 用户模块 `user`
  * 字典模块 `dictionary`
    * 字典类型模块 `type`
      * 首页面 `index.jsp`
      * 修改页面
      * 新增页面
    * 字典值模块 `value`
      * 首页面 `index.jsp`
      * 修改页面
      * 新增页面
* 工作台模块 `workbench`

## 字典模块介绍
> 为了给页面的下拉框提供一个修改的入口,那么我们的字典模块就应运而生了

## 加载字典类型列表数据
* 后台代码
```java
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
```

* jsp页面
```html
<table class="table table-hover">
    <thead>
        <tr style="color: #B3B3B3;">
            <td><input id="selectAllBtn" type="checkbox" /></td>
            <td>序号</td>
            <td>编码</td>
            <td>名称</td>
            <td>描述</td>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${dictionaryTypeList}" var="dt" varStatus="dts">
            <tr class="${dts.index%2==0?'active':''}">
                <td><input type="checkbox" name="flag"/></td>
                <td>${dts.count}</td>
                <td>${dt.code}</td>
                <td>${dt.name}</td>
                <td>${dt.description}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>
```

## 字典类型首页面的全选和反选操作
```javascript
function selectAll() {
    $("#selectAllBtn").click(function () {
        //根据全选框的选中状态,加载所有的复选框的选中状态
        $("input[name=flag]").prop("checked",this.checked)
    })
}


function reverseAll() {
    $("input[name=flag]").click(function () {
        //所有的复选框都选中之后,默认选中全选框
        $("#selectAllBtn").prop("checked",$("input[name=flag]").length == $("input[name=flag]:checked").length)
    })
}
```

## 新增字典类型操作
### 校验编码是否存在
* 前端代码
```javascript
/*
    校验编码操作
        当前唯一一个在页面中添加主键的功能
 */
function checkCode() {
    //当输入框失去焦点后,我们需要触发,并发送请求查询数据库,得知结果编码是否存在
    $("#code").blur(function () {
        //获取编码内容
        let code = $("#code").val();

        if(code == ""){
            //大家可以选择,弹出框或者页面弹出提示信息
            $("#msg").html("编码不能为空");
            return;
        }

        //发送请求,查询编码是否存在
        get(
            "settings/dictionary/type/checkCode.do",
            {code:code},
            data=>{
                // if(data.success){
                //     //请求成功,编码可以新增
                // }else
                //     //编码存在,不能新增
                //     $("#msg").html(data.msg);

                if(!data.success)
                    //编码存在,不能新增
                    $("#msg").html(data.msg);
                else
                    //编码可以新增,清空提示信息
                    $("#msg").html("");
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/type/checkCode.do")
@ResponseBody
public R checkCode(@RequestParam("code") String code) {
    //可以查询数据库,查询编码是否存在
    //可以通过编码查询字典类型的数量
    //select count(id) from tbl_dic_type where code = #{code} -> 返回值是int类型
    //也可以通过编码查询字典类型对象
    //select * from tbl_dic_type where code = #{code} -> 返回值是对象
    DictionaryType dictionaryType = dictionaryService.findDictionaryType(code);

    //数据库没有查询到当前的字典类型对象,证明当前的编码是可以新增的
    if (ObjectUtils.isEmpty(dictionaryType))
        return R.builder()
                .code(State.SUCCESS.getCode())
                .msg(State.SUCCESS.getMsg())
                .success(true)
                .build();
    else
        //数据库查询到了当前的编码对应的字典类型对象,不能够新增
        return R.builder()
                .code(State.DB_FIND_EXISTS_ERROR.getCode())
                .msg(State.DB_FIND_EXISTS_ERROR.getMsg())
                .success(false)
                .build();
}
```

### 新增操作
* 前端代码
```javascript
function saveDictionaryType() {
    $("#saveDictionaryTypeBtn").click(function () {
        //获取编码,名称,描述信息
        let code = $("#code").val();
        let name = $("#name").val();
        let description = $("#description").val();

        //这里不能够使用val方法进行获取,因为span的文本内容是我们的错误信息
        //<span>错误信息文本</span>
        let errMsg = $("#msg").html();

        //校验编码是否存在,它是必传的参数
        if(code == ""){
            $("#msg").html("编码不能为空");
            return;
        }

        if(errMsg != "")
            //有错误信息没有解决
            return;

        //发送请求,新增操作,以json的方式进行参数传递
        post(
            "settings/dictionary/type/saveDictionaryType.do",
            {
                code:code,
                name:name,
                description:description,
            },data=>{
                // if(data.success){
                //
                // }else{
                //     alert(data.msg);
                // }

                if(checked(data)) return;

                //新增成功,跳转到字典类型列表页面
                to("settings/dictionary/type/toIndex.do");
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/type/saveDictionaryType.do")
@ResponseBody
//public R saveDictionaryType(@RequestBody Map<String,String> dictionaryType){
public R saveDictionaryType(@RequestBody DictionaryType dictionaryType) {
    //校验参数信息
    if (StringUtils.isBlank(dictionaryType.getCode()))
        throw new RuntimeException(State.PARAMS_ERROR.getMsg());

    //校验通过,新增字典类型
    //在配置文件中通过Spring声明式事务控制来进行事务的操作
    //service的命名方法,必须以save update delete开头
    boolean flag = dictionaryService.saveDictionaryType(dictionaryType);

    return flag
            ?
            R.builder()
            .code(State.SUCCESS.getCode())
            .msg(State.SUCCESS.getMsg())
            .success(true)
            .build()
            :
            R.builder()
            .code(State.DB_SAVE_ERROR.getCode())
            .msg(State.DB_SAVE_ERROR.getMsg())
            .success(false)
            .build();
}
```


## 修改字典类型操作
### 回显数据
* 前端代码
```javascript
function toEdit() {
    //给编辑按钮,添加点击事件
    $("#toEditBtn").click(function () {
        //当点击了编辑按钮后,我们需要获取选中的复选框的标签对象(数组,集合)
        let flags = $("input[name=flag]:checked");

        //判断是否选中了一条记录
        if(flags.length != 1){
            //选中了多个或没有选中
            /*
                什么时候用alert
                    页面的内容数据较多时,alert方式的提示较为明显
                    比如列表页面...
                什么时候用标签来提示
                    页面的数据较少时,通过标签来提示,比较醒目
                    比如新增页面...
             */
            alert("请选择一条需要修改的数据");
            return;
        }

        //校验通过
        //获取复选框中的value属性(编码内容),来发送请求进行查询
        let code = flags[0].value;

        //我们直接进行发送请求跳转页面即可,不需要发送异步的请求
        //要在后台控制器中,直接进行页面的跳转
        to("settings/dictionary/type/toEdit.do?code="+code);
    })
}
```

* 后台代码
```java
/*
    根据code查询字典类型数据,并跳转到修改页面操作
 */
@RequestMapping("/type/toEdit.do")
public String toTypeEdit(@RequestParam("code")String code,Model model){
    //通过code查询字典类型对象
    DictionaryType dictionaryType = dictionaryService.findDictionaryType(code);

    //如果字典类型对象不为空,存入到Model对象中,携带到页面进行加载
    if(ObjectUtils.isNotEmpty(dictionaryType))
        model.addAttribute("dictionaryType",dictionaryType);

    //跳转到字典类型的修改页面
    return "/settings/dictionary/type/edit";
}
```

### 修改操作
* 前端代码
```javascript
function updateDictionaryType() {
    //给更新按钮,添加点击事件
    $("#updateDictionaryTypeBtn").click(function () {
        //获取页面中的属性信息
        let code = $("#code").val();
        let name = $("#name").val();
        let description = $("#description").val();

        //虽然页面中是只读的输入框,无法修改数据,但是我们可以通过代码的方式进行侵入
        //在这个地方为了提高代码的健壮性,必须要进行业务逻辑的判断
        if(code == ""){
            $("#msg").html("页面数据加载异常,请刷新后再试");
            return;
        }

        //发送post请求,进行修改操作的提交
        post(
            "settings/dictionary/type/updateDictionaryType.do",
            {
                code:code,
                name:name,
                description:description,
            },data=>{
                if(checked(data)) return;
                //修改成功,跳转到字典类型列表页面
                to("settings/dictionary/type/toIndex.do");
            }
        )
    })
}
```

* 后台代码
```java
/*
    修改字典类型操作
        一定要记住接收参数的注解的使用
            @RequestBody
            @RequestParam
 */
@RequestMapping("/type/updateDictionaryType.do")
@ResponseBody
public R updateDictionaryType(@RequestBody DictionaryType dictionaryType){
    //校验参数的合法性
    checked(
            //只校验必传的参数,而不是校验所有参数信息
            dictionaryType.getCode()
    );

    //更新操作
    boolean flag = dictionaryService.updateDictionaryType(dictionaryType);

    return flag ? ok() : err(State.DB_UPDATE_ERROR);
}
```

* Sql
```xml
<update id="update">
    update tbl_dic_type
        <set>
            <if test="name != null and name != ''">
                name = #{name} ,
            </if>
            <if test="description != null and description != ''">
                description = #{description} ,
            </if>
        </set>
        where code = #{code}
</update>
```