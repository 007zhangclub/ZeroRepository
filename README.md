# Crm项目笔记

## 加载转换页面基本数据

* 前端代码

```javascript
<button type="button" class="btn btn-default"
        onclick="window.location.href='workbench/clue/toConvert.do?clueId=${clue.id}';">
    <span class="glyphicon glyphicon-retweet"></span>
    转换
</button>
```

* 后台代码

```java
@RequestMapping("/toConvert.do")
public String toConvert(@RequestParam("clueId")String clueId,Model model){
        //根据线索id查询线索数据
        Clue clue=clueService.findClue(clueId);

        if(ObjectUtils.isNotEmpty(clue))
        model.addAttribute("clue",clue);

        return"/workbench/clue/convert";
        }
```

## 加载已关联的市场活动列表数据

* 前端代码

```javascript
function openSearchActivityModal() {
    $("#openSearchActivityModalBtn").click(function () {
        getRelationList();
        //打开模态窗口
        $("#searchActivityModal").modal("show");
    })
}

function getRelationList() {
    //发送请求获取数据
    get(
        "workbench/clue/getClueActivityRelationList.do",
        {
            clueId: $("#clueId").val(),
            activityName: $("#searchActivityInput").val()
        }, data => {
            if (checked(data))
                return;

            //异步加载
            load(
                $("#activityListBody"),
                data,
                (i, n) => {
                    return '<tr>' +
                        '<td><input type="radio" name="activity" value="' + n.id + '"/></td>' +
                        '<td id="n_' + n.id + '">' + n.name + '</td>' +
                        '<td>' + n.startDate + '</td>' +
                        '<td>' + n.endDate + '</td>' +
                        '<td>' + n.username + '</td>' +
                        '</tr>';
                }
            )
        }
    )
}
```

* 后台代码

```java
@RequestMapping("/getClueActivityRelationList.do")
@ResponseBody
public R getClueActivityRelationList(@RequestParam("clueId") String clueId,
@RequestParam(value = "activityName", required = false)String activityName){
        //根据线索id查询当前线索已关联的市场活动列表数据
        List<Activity> activityList=clueService.findClueActivityRelationList(clueId,activityName);

        return ok(activityList);
        }
```

* Sql

```xml

<select id="findActivityRelationListLike" resultType="com.bjpowernode.crm.workbench.domain.Activity">
    select car.id as carId, u.name as username, a.*
    from tbl_activity a
    left join tbl_user u on a.owner = u.id
    left join tbl_clue_activity_relation car on a.id = car.activityId
    where a.isDelete = '0'
    and car.clueId = #{clueId}
    <if test="activityName != null and activityName != ''">
        and a.name like '%' #{activityName} '%'
    </if>
</select>
```

## 模糊查询已关联的市场活动列表数据

* 前端代码

```javascript
function searchActivityList() {
    $("#searchActivityInput").keydown(function (event) {
        if (event.keyCode == 13) {
            getRelationList();

            return false;
        }
    })
}
```

## 回显市场活动数据

```javascript
function showActivity() {
    $("#addRelationBtn").click(function () {
        //获取当前已选中的市场活动数据
        let activities = $("input[name=activity]:checked");

        if (activities.length != 1) {
            //没有选中或选中多条记录
            alert("请选择一条需要关联的记录");
            return;
        }

        //获取市场活动id
        let activityId = activities[0].value;

        //获取市场活动名称
        let activityName = $("#n_" + activityId).html();

        //回显数据,将id存入到隐藏域中
        $("#activityId").val(activityId);

        $("#activity").val(activityName);

        //关闭模态窗口
        $("#searchActivityModal").modal("hide");
    })
}
```

## 线索转换操作
* 页面
```html

<form method="post" action="workbench/clue/clueConvert.do?clueId=${clue.id}" id="convertForm">
    <input type="hidden" name="activityId" id="activityId">
    <div class="form-group" style="width: 400px; position: relative; left: 20px;">
        <label for="amountOfMoney">金额</label>
        <input type="text" class="form-control" name="money" id="money">
    </div>
    <div class="form-group" style="width: 400px;position: relative; left: 20px;">
        <label for="tradeName">交易名称</label>
        <input type="text" class="form-control" name="name" id="name">
    </div>
    <div class="form-group" style="width: 400px;position: relative; left: 20px;">
        <label for="expectedClosingDate">预计成交日期</label>
        <input type="text" class="form-control time1" autocomplete="off" name="expectedDate" id="expectedDate">
    </div>
    <div class="form-group" style="width: 400px;position: relative; left: 20px;">
        <label for="stage">阶段</label>
        <select id="stage" name="stage" class="form-control">
            <option></option>
            <c:forEach items="${stage}" var="ss">
                <option value="${ss.value}">${ss.text}</option>
            </c:forEach>
        </select>
    </div>
    <div class="form-group" style="width: 400px;position: relative; left: 20px;">
        <label for="activity">市场活动源&nbsp;&nbsp;<a href="javascript:void(0);" id="openSearchActivityModalBtn"
                                                  style="text-decoration: none;"><span
                class="glyphicon glyphicon-search"></span></a></label>
        <input type="text" class="form-control" id="activity" placeholder="点击上面搜索" readonly>
    </div>
</form>
```

* 前端代码
```javascript
function clueConvert() {
    $("#clueConvertBtn").click(function () {
        //根据复选框的选中状态,来决定是创建交易的线索转换还是没有创建交易的线索转换操作
        if ($("#isCreateTransaction").prop("checked"))
            //选中,创建交易的线索转换操作
            $("#convertForm").submit();
        else
            //未选中,没有创建交易的线索转换操作
            to("workbench/clue/clueConvert.do?clueId=" + $("#clueId").val())
    })
}
```

* 线索转换后台业务梳理
```java
/*
    线索转换操作业务逻辑梳理:
        1. 根据线索id,查询线索数据
        2. 获取线索数据中的客户名称(company)和线索中的联系人名称(fullname)
        3. 根据客户名称查询是否有当前客户信息
            如果查询出的结果是null,新增客户信息
            如果有当前的客户信息,直接使用即可
        4. 根据联系人名称查询是否有当前联系人信息
            如果查询出的结果是null,新增联系人信息
            如果有当前的联系人信息,直接使用即可
        -----------------一对一的线索转换操作-----------------
        5. 根据线索id,查询线索关联的备注信息列表数据
            如果线索备注信息列表不为null,我们需要将线索备注信息列表转换为客户备注信息列表和联系人的备注信息列表
        -----------------一对多的线索转换操作-----------------
        6. 根据线索id,查询线索关联的中间表数据(线索和市场活动的中间表数据)
            如果线索关联的中间表数据不为null,我们需要将线索和市场活动的中间表数据,转换为联系人和市场活动的中间表数据
        -----------------多对多的线索转换操作-----------------
        7. 是否需要新增交易记录
            根据flag或者tran是否为null,来判断是否需要新增交易记录
                如果需要新增交易记录,那么我们则新增交易和交易历史记录
            如果无需创建交易,跳过此步骤即可
        8. 根据线索id,删除线索和市场活动的中间表记录
        9. 根据线索id,删除线索备注信息列表数据
        10. 根据线索id,删除线索数据

    表结构的梳理:
        线索的一对一转换
            tbl_clue -> tbl_customer 和 tbl_contacts
        线索的一对多转换
            tbl_clue_remark -> tbl_customer_remark 和 tbl_contacts_remark
        线索的多对多转换
            tbl_clue_activity_relation -> tbl_contacts_activity_relation
        数据已经转换到其他表中了,所以我们转换完成的最后就可以将线索和线索相关的表的数据删除掉
 */
```

* 后台代码 `controller`
```java
/*
    线索转换操作
        clueId
        flag
        Tran
            activityId
            name
            money
            stage
            expectedDate
        如果tran为null证明是没有创建交易的线索转换
        或者单独传递一个标记,代表是否创建交易的线索转换操作
 */
@RequestMapping("/clueConvert.do")
public String clueConvert(@RequestParam("clueId")String clueId,
                          @RequestParam(value = "flag",required = false)String flag,
                          Tran tran){
    //线索转换操作
    clueService.saveClueConvert(
            clueId,flag,tran,getOwner(),getName(),getTime()
    );
    
    return "/workbench/clue/index";
}
```

