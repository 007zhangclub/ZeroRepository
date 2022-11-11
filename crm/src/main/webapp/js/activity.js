/*
    将模糊查询和列表查询整合到一起
 */
function getActivityListPage(pageNo, pageSize) {

    //获取模糊查询的条件
    let name = $("#search-name").val();
    let owner = $("#search-owner").val();
    let startDate = $("#search-startDate").val();
    let endDate = $("#search-endDate").val();

    get(
        "workbench/activity/getActivityListPage.do",
        {
            pageNo:pageNo,
            pageSize:pageSize,
            name:name,
            owner:owner,
            startDate:startDate,
            endDate:endDate,
        },data=>{
            //这里的返回值,稍微不同
            //因为列表查询时,可能存在列表为空的问题,所以这里我们也可以不返回code,success,msg属性
            //可以直接返回分页相关的数据
            loadPage(
                $("#activityListBody"),
                data,
                (i,n) => {
                    return  '<tr class="active">'+
                            '<td><input type="checkbox" name="flag" value="'+n.id+'"/></td>'+
                            '<td><a style="text-decoration: none; cursor: pointer;" onClick="window.location.href=\'detail.html\';">'+n.name+'</a></td>'+
                            '<td>'+n.username+'</td>'+
                            '<td>'+n.startDate+'</td>'+
                            '<td>'+n.endDate+'</td>'+
                            '</tr>';
                }
            )

            //根据分页查询的结果,初始化并加载前端的分页组件
            $("#activityPage").bs_pagination({
                currentPage: data.pageNo, // 页码
                rowsPerPage: data.pageSize, // 每页显示的记录条数
                maxRowsPerPage: 20, // 每页最多显示的记录条数
                totalPages: data.totalPages, // 总页数
                totalRows: data.totalCounts, // 总记录条数

                visiblePageLinks: 3, // 显示几个卡片

                showGoToPage: true,
                showRowsPerPage: true,
                showRowsInfo: true,
                showRowsDefaultInfo: true,
                //当触发分页组件时的回调方法,那么当点击了分页组件的按钮时,我们发送请求,调用分页方法即可
                onChangePage : function(event, data){
                    getActivityListPage(data.currentPage , data.rowsPerPage);
                }
            })
        }
    )
}


function searchActivity() {
    $("#searchActivityBtn").click(function () {
        getActivityListPage(1,5);
    })
}



function initDateTimePicker() {
    //加载一个日历控件的样式
    $(".time").datetimepicker({
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


function openCreateActivityModal() {
    $("#openCreateActivityModalBtn").click(function () {
        //发送请求,获取数据
        get(
            "settings/user/getUserList.do",
            {},
            data=>{
                if(checked(data))
                    return;
                //异步加载
                loadHtml(
                    $("#create-owner"),
                    data,
                    (i,n) =>{
                        return "<option value="+n.id+">"+n.name+"</option>"
                    },
                    "<option></option>"
                )

                //默认选中当前登录的用户
                $("#create-owner").val($("#userId").val());

                //打开模态窗口
                $("#createActivityModal").modal("show");
            }
        )
    })
}


function saveActivity() {
    $("#saveActivityBtn").click(function () {
        //获取市场活动属性信息
        let name = $("#create-name").val();
        let owner = $("#create-owner").val();
        let startDate = $("#create-startDate").val();
        let endDate = $("#create-endDate").val();
        let cost = $("#create-cost").val();
        let description = $("#create-description").val();

        //校验
        if(name == ""){
            alert("活动名称不能为空");
            return;
        }

        if(owner == ""){
            alert("所有者不能为空");
            return;
        }

        //发送post请求,新增数据
        post4m(
            "workbench/activity/saveActivity.do",
            {
                name:name,
                owner:owner,
                startDate:startDate,
                endDate:endDate,
                cost:cost,
                description:description,
            },data=>{
                if(checked(data))
                    return;

                /*
                    现在页面中的列表数据我们是异步加载的,那么触发这个分页加载的时机很多
                        1. 进入页面,加载分页数据
                        2. 条件过滤查询,加载分页数据
                        3. 触发前端分页组件,加载分页数据
                        4. 增删改市场活动操作,加载分页数据
                        5. 批量的导入,加载分页数据
                 */
                //新增成功,刷新列表数据,关闭模态窗口
                getActivityListPage(1,5);

                $("#createActivityModal").modal("hide");
            }
        )
    })
}


function openEditActivityModal() {
    $("#openEditActivityModalBtn").click(function () {
        //获取当前选中的市场活动的标签数据
        let flags = $("input[name=flag]:checked");

        if(flags.length != 1){
            //要么没有选中,要么选中多条记录
            alert("请选择一条需要修改的数据");
            return;
        }

        //获取所有者下拉列表数据,发送请求回显数据
        get(
            "settings/user/getUserList.do",
            {},
            data=>{
                if(checked(data)) return;

                //回显数据
                load(
                    $("#edit-owner"),
                    data,
                    (i,n) =>{
                        return "<option value="+n.id+">"+n.name+"</option>"
                    }
                )

                //加载成功后,获取市场活动的id,发送请求回显数据
                get(
                    "workbench/activity/getActivity.do",
                    {id:flags[0].value},
                    data=>{
                        if(checked(data)) return;

                        //回显数据
                        //根据用户id回显所有者下拉框中的数据,默认选中
                        $("#edit-owner").val(data.data.owner);
                        $("#edit-name").val(data.data.name);
                        $("#edit-startDate").val(data.data.startDate);
                        $("#edit-endDate").val(data.data.endDate);
                        $("#edit-cost").val(data.data.cost);
                        $("#edit-description").val(data.data.description);
                        //将修改的id,存入到隐藏域中,为后续的修改操作做铺垫
                        $("#edit-id").val(data.data.id);

                        //打开修改的模态窗口
                        $("#editActivityModal").modal("show");
                    }
                )
            }
        )
    })
}



function updateActivity() {
    $("#updateActivityBtn").click(function () {
        let id = $("#edit-id").val();
        let owner = $("#edit-owner").val();
        let name = $("#edit-name").val();
        let startDate = $("#edit-startDate").val();
        let endDate = $("#edit-endDate").val();
        let cost = $("#edit-cost").val();
        let description = $("#edit-description").val();

        if(id == ""){
            alert("页面加载异常,请刷新后再试");
            return;
        }

        if(name == ""){
            alert("活动名称不能为空");
            return;
        }

        if(owner == ""){
            alert("所有者不能为空");
            return;
        }

        //发送post请求修改操作
        post(
            "workbench/activity/updateActivity.do",
            {
                id:id,
                owner:owner,
                name:name,
                startDate:startDate,
                endDate:endDate,
                cost:cost,
                description:description,
            },data=>{
                if(checked(data))
                    return;

                //修改成功,刷新列表,关闭模态窗口
                getActivityListPage(1,5);

                $("#editActivityModal").modal("hide");
            }
        )
    })
}



function batchDelete() {
    $("#batchDeleteBtn").click(function () {
        //获取页面中选中的复选框对象
        let flags = $("input[name=flag]:checked");

        if(flags.length == 0){
            alert("请选择需要删除的市场活动数据")
            return;
        }

        //console.log(params);
        //由于删除是一个危险的动作,我们需要给出提示
        if(confirm("确定删除吗?")){
            //没有问题,拼接参数发送请求
            //url?ids=xxx&ids=xxx...
            let params = "";

            for(let i=0; i<flags.length; i++){
                //这样拼接最后会多出一个&,但是不影响功能
                //params += "ids=" + flags[i].value + "&";
                params += "ids=" + flags[i].value;

                if(i < flags.length - 1) params += "&";
            }

            //发送get请求
            get(
                "workbench/activity/deleteActivityList.do?"+params,
                {},
                data=>{
                    if(checked(data))
                        return;

                    //删除成功,刷新列表数据
                    getActivityListPage(1,5);
                }
            )
        }
    })
}


function importActivity() {
    //当点击导入按钮时,我们直接提交表单数据即可
    $("#importActivityBtn").click(function (){
        $("#uploadForm").submit();
    })
}


function exportActivityAll() {
    $("#exportActivityAllBtn").click(function () {
        //需要给出一个提示操作,以免用户误操作,直接下了文件
        if(confirm("确定要导出全部数据吗?"))
            //这里不需要异步发送请求了,因为我们要通过response对象来响应下载的文件
            to("workbench/activity/exportActivity.do");
    })
}