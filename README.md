# Crm项目笔记

## 加载市场活动分页数据
* 前端代码
```javascript
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
```

* 后台代码
```java
@RequestMapping("/getActivityListPage.do")
@ResponseBody
public R getActivityListPage(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                @RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "owner", required = false) String owner,
                                @RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate) {

    //根据分页条件来进行查询数据
    /*
        pageNo      分页参数
        pageSize    分页参数
        name        模糊查询
        owner       等值查询
        startDate   大于等于
        endDate     小于等于
     */
    List<Activity> activityList = activityService.findPage(
            pageNo,
            pageSize,
            name,
            owner,
            startDate,
            endDate
    );

    /*
        查询总记录数和计算总页数
     */
    int totalCounts = activityService.findPageCount(
            name,
            owner,
            startDate,
            endDate
    );

    int totalPages = totalCounts % pageSize == 0 ? totalCounts / pageSize : (totalCounts / pageSize) + 1 ;

    return new Page()
            .setRecords(activityList)
            .setTotalCounts(totalCounts)
            .setTotalPages(totalPages)
            .setPageNo(pageNo)
            .setPageSize(pageSize);
            //.setCode(State.SUCCESS.getCode())
            //.setMsg(State.SUCCESS.getMsg())
            //.setSuccess(true);
}
```

* Sql
```xml
<select id="findPage" resultType="com.bjpowernode.crm.workbench.domain.Activity">
    select a.*,u.name username from tbl_activity a left join tbl_user u on a.owner = u.id
    <where>
        a.isDelete = '0'
        <if test="name != null and name != ''">
            and a.name like '%' #{name} '%'
        </if>

        <if test="owner != null and owner != ''">
            and u.name = #{owner}
        </if>

        <if test="startDate != null and startDate != ''">
            and a.startDate &gt;= #{startDate}
        </if>

        <if test="endDate != null and endDate != ''">
            and a.endDate &lt;= #{endDate}
        </if>
    </where>
    limit #{pageNo},#{pageSize}
</select>
<select id="findPageCount" resultType="java.lang.Integer">
    select count(a.id) from tbl_activity a left join tbl_user u on a.owner = u.id
    <where>
        a.isDelete = '0'
        <if test="name != null and name != ''">
            and a.name like '%' #{name} '%'
        </if>

        <if test="owner != null and owner != ''">
            and u.name = #{owner}
        </if>

        <if test="startDate != null and startDate != ''">
            and a.startDate &gt;= #{startDate}
        </if>

        <if test="endDate != null and endDate != ''">
            and a.endDate &lt;= #{endDate}
        </if>
    </where>
</select>
```


## 条件过滤查询
* 前端代码
```javascript
function searchActivity() {
    $("#searchActivityBtn").click(function () {
        getActivityListPage(1,5);
    })
}
```


## 加载日历控件
```javascript
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
```