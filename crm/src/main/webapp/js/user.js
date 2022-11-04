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
                //if(data.success)
            }
        )
    })
}