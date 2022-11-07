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
        }
    )
}