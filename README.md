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


## 认识模态窗口
> 带有动画和渐变效果的html标签
* 模态窗口的打开和关闭
```html
<!--
页面中打开和关闭
    打开 data-toggle="modal" data-target="#createActivityModal"
    关闭 data-dismiss="modal"
-->
<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#createActivityModal"><span class="glyphicon glyphicon-plus"></span> 创建</button>
<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
```

```javascript
//js中通过代码控制打开和关闭
//打开模态窗口
$("#createActivityModal").modal("show");
//关闭模态窗口
$("#createActivityModal").modal("hide");
```

## 新增市场活动
### 加载所有者下拉列表数据
* 前端代码
```javascript
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
```

* 后台代码
```java
@RequestMapping("/getUserList.do")
@ResponseBody
public R getUserList(){
    return okAndCheck(
            userService.findUserList()
    );
}
```

### 新增操作
* 前端代码
```javascript
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
```

* 后台代码
```java
@RequestMapping("/saveActivity.do")
@ResponseBody
public R saveActivity(Activity activity, HttpSession session){
    /*
        我们在系统设置模块中,抽取了一个父类Settings
            我们将一些校验的方法,放到了这个父类中,进行调用,这样很方便
        现在我们写到了工作台模块,那么又需要用到这些方法,我们怎么办呢?
            1. 创建一个父类,将代码复制过来
            2. 将当前类继承自Settings
        *** 3. 创建一个公共的基类,再创建一个Workbench和Settings共同实现这个父类
                将公共的代码放到基类中
     */
    checked(
            activity.getOwner(),
            activity.getName()
    );


    //String name = ((User) session.getAttribute("user")).getName();
    //String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    //封装实体类的id,createBy,createTime,editBy,editTime这些属性
    //新增的市场活动默认是未删除的数据
    activity.setId(getId())
            .setCreateBy(getName())
            .setCreateTime(getTime())
            .setEditBy(getName())
            .setEditTime(getTime())
            .setIsDelete("0");

    //新增操作
    boolean flag = activityService.saveActivity(activity);

    return ok(flag);
}
```

* Sql
```xml
<sql id="star">
    id,
    owner,
    name,
    startDate,
    endDate,
    cost,
    description,
    createTime,
    createBy,
    editTime,
    editBy,
    isDelete
</sql>
<sql id="starValues">
    #{id},
    #{owner},
    #{name},
    #{startDate},
    #{endDate},
    #{cost},
    #{description},
    #{createTime},
    #{createBy},
    #{editTime},
    #{editBy},
    #{isDelete}
</sql>
<insert id="insert">
    insert into tbl_activity
    (
     <include refid="star"/>
    )
    values
    (
     <include refid="starValues"/>
    )
</insert>
```

## 修改市场活动操作
### 打开模态窗口回显数据
> 发送两次请求
* 前端代码
```javascript
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
```

* 后台代码
```java
@RequestMapping("/getActivity.do")
@ResponseBody
public R getActivity(@RequestParam("id")String id){
    return ok(
            activityService.findActivity(id)
    );
}
```

* Sql
```java
<select id="findById" resultType="com.bjpowernode.crm.workbench.domain.Activity">
    select a.*,u.name username from tbl_activity a left join tbl_user u on a.owner = u.id
    where a.id = #{id}
</select>
```

### 修改操作