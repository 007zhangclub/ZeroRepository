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
                            '<td><input type="checkbox"/></td>'+
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