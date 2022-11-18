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



function loadPossibilityByStage(json) {
    //给下拉框绑定事件,当选中了其他的选项时,根据当前的选项的名称,加载对应的可能性数据到只读的输入框中
    $("#create-stage").change(function () {
        //获取当前选中的选项数据(阶段名称)
        let stage = $(this).val();

        //根据阶段名称加载可能性数据
        let possibility = json[stage];

        $("#create-possibility").val(possibility);
    })
}


function openFindMarketActivity() {
    $("#openFindMarketActivityBtn").click(function () {
        //获取数据,异步加载
        get(
            "workbench/activity/getActivityList.do",
            {
                activityName:$("#activityName").val()
            },data=>{
                if(checked(data))
                    return;
                //异步加载
                getActivityList(data);

                //打开模态窗口
                $("#findMarketActivity").modal("show");
            }
        )
    })
}


function getActivityList(data) {
    load(
        $("#activityListBody"),
        data,
        (i,n)=>{
            return  '<tr>'+
                    '<td><input type="radio" name="activity" value="'+n.id+'"/></td>'+
                    '<td>'+n.name+'</td>'+
                    '<td>'+n.startDate+'</td>'+
                    '<td>'+n.endDate+'</td>'+
                    '<td>'+n.username+'</td>'+
                    '</tr>';
        }
    )
}