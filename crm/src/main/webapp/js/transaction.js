function initDateTimePicker() {
    //加载一个日历控件的样式
    $(".time1").datetimepicker({
        //最小的视图是月份
        minView: "month",
        //中文显示
        language:  'zh-CN',
        //提交参数的日期格式
        format: 'yyyy-mm-dd',
        //是否支持自动关闭
        autoclose: true,
        //是否支持今天按钮,点击后可以加载到今天的日期
        todayBtn: true,
        //组件显示位置,左下方
        pickerPosition: "top-left"
    });

    $(".time2").datetimepicker({
        //最小的视图是月份
        minView: "month",
        //中文显示
        language:  'zh-CN',
        //提交参数的日期格式
        format: 'yyyy-mm-dd',
        //是否支持自动关闭
        autoclose: true,
        //是否支持今天按钮,点击后可以加载到今天的日期
        todayBtn: true,
        //组件显示位置,左下方
        pickerPosition: "bottom-left"
    });
}


function autoLoadCustomerName() {
    //给输入框对象绑定自动补全事件
    $("#create-customerName").typeahead({
        //query代表输入的关键字
        //process代表将返回值结果进行解析,回显到输入框中
        source:function (query,process) {
            get(
                "workbench/transaction/getCustomerName.do",
                {customerName:query},
                data=>{
                    if(checked(data)) return;

                    //返回值的数据是List<String>,客户名称的列表数据
                    process(data.data);
                }
            )
        },
        //当输入内容时,每隔多久发送一次请求
        delay:500
    })
}