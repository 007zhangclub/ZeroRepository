# Crm项目笔记
## 新增交易操作
### 加载市场活动源数据
* 前端代码
```javascript
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
```

* 后台代码
```java
@RequestMapping("/getCustomerName.do")
@ResponseBody
public R<List<String>> getCustomerName(@RequestParam("customerName")String customerName){
    //根据客户名称,模糊查询获取客户名称的列表数据,仅限于名称,其他不需要
    List<String> customerNameList = customerService.findCustomerNameList(customerName);

    return ok(customerNameList);
}
```

* 回显数据
```javascript
function addActivity() {
    $("#addActivityBtn").click(function () {
        let activity = $("input[name=activity]:checked");

        if(activity.length == 0){
            alert("请选择需要关联的市场活动数据");
            return;
        }

        let activityId = activity[0].value;

        let activityName = $("#n_"+activityId).html();

        //将id存入到隐藏域中
        $("#create-activityId").val(activityId);

        //将名称回显到只读输入框中
        $("#create-activitySrc").val(activityName);

        $("#findMarketActivity").modal("hide");
    })
}
```

### 新增交易
* 前端代码
```javascript
function saveTransaction() {
    $("#saveTransactionBtn").click(function () {
        //获取页面中的属性信息
        let owner = $("#create-owner").val();
        let name = $("#create-name").val();
        let expectedDate = $("#create-expectedDate").val();
        let customerName = $("#create-customerName").val();
        let stage = $("#create-stage").val();
        let money = $("#create-money").val();
        let type = $("#create-type").val();
        let possibility = $("#create-possibility").val();
        let source = $("#create-source").val();
        let activityId = $("#create-activityId").val();
        let contactsId = $("#create-contactsId").val();
        let description = $("#create-description").val();
        let contactSummary = $("#create-contactSummary").val();
        let nextContactTime = $("#create-nextContactTime").val();

        //校验
        if(owner == ""){
            alert("所有者不能为空");
            return;
        }

        if(name == ""){
            alert("名称不能为空");
            return;
        }

        if(expectedDate == ""){
            alert("成交日期不能为空");
            return;
        }

        if(customerName == ""){
            alert("客户名称不能为空");
            return;
        }

        if(stage == ""){
            alert("阶段不能为空");
            return;
        }

        //发送请求,新增记录
        post(
            "workbench/transaction/saveTransaction.do?customerName="+customerName,
            {
                owner:owner,
                name:name,
                expectedDate:expectedDate,
                customerName:customerName,
                stage:stage,
                money:money,
                type:type,
                possibility:possibility,
                source:source,
                activityId:activityId,
                contactsId:contactsId,
                description:description,
                contactSummary:contactSummary,
                nextContactTime:nextContactTime
            },data=>{
                if(checked(data))
                    return;
                //新增成功后,跳转回交易首页面
                to(
                    "workbench/transaction/toIndex.do"
                )
            }
        )
    })
}
```

* 后台代码 `controller`
```java
@RequestMapping("/saveTransaction.do")
@ResponseBody
public R<Boolean> saveTransaction(@RequestBody Tran tran,
                                  @RequestParam(value = "customerName")String customerName){
    //校验
    checked(
            tran.getOwner(),
            tran.getStage(),
            tran.getName(),
            tran.getExpectedDate(),
            customerName
    );

    //新增交易操作
    boolean flag = transactionService.saveTransaction(tran,customerName,getName(),getTime());

    return ok(flag);
}
```

* 后台代码 ```service```
```java
@Override
public boolean saveTransaction(Tran tran, String customerName, String name, String time) {

    //根据客户名称,查询当前客户信息
    Customer customer = customerDao.findByName(customerName);

    if (ObjectUtils.isEmpty(customer)) {
        //新增客户信息
        customer = Customer.builder()
                .id(IdUtils.getId())
                .owner(tran.getOwner())
                .name(customerName)
                //.website()
                //.phone()
                .createBy(name)
                .createTime(time)
                .editBy(name)
                .editTime(time)
                .contactSummary(tran.getContactSummary())
                .nextContactTime(tran.getNextContactTime())
                .description(tran.getDescription())
                //.address()
                .build();

        customerDao.insert(customer);
    }

    //给交易赋值
    tran.setCustomerId(customer.getId())
            .setId(IdUtils.getId())
            .setCreateBy(name)
            .setCreateTime(time)
            .setEditBy(name)
            .setEditTime(time);

    //新增交易
    int a = tranDao.insert(tran);

    if (a <= 0)
        throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

    //新增交易历史记录
    TranHistory history = TranHistory.builder()
            .id(IdUtils.getId())
            .stage(tran.getStage())
            .money(tran.getMoney())
            .expectedDate(tran.getExpectedDate())
            .createTime(time)
            .createBy(name)
            .tranId(tran.getId())
            .build();

    tranHistoryDao.insert(history);

    return a > 0;
}
```
