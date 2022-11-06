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