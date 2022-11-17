# Crm项目笔记
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