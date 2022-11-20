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
                activityName: $("#activityName").val()
            }, data => {
                if (checked(data))
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
        (i, n) => {
            return '<tr>' +
                '<td><input type="radio" name="activity" value="' + n.id + '"/></td>' +
                '<td>' + n.name + '</td>' +
                '<td>' + n.startDate + '</td>' +
                '<td>' + n.endDate + '</td>' +
                '<td>' + n.username + '</td>' +
                '</tr>';
        }
    )
}
```

* 后台代码

```java
@RequestMapping("/getCustomerName.do")
@ResponseBody
public R<List<String>>getCustomerName(@RequestParam("customerName")String customerName){
        //根据客户名称,模糊查询获取客户名称的列表数据,仅限于名称,其他不需要
        List<String> customerNameList=customerService.findCustomerNameList(customerName);

        return ok(customerNameList);
        }
```

* 回显数据

```javascript
function addActivity() {
    $("#addActivityBtn").click(function () {
        let activity = $("input[name=activity]:checked");

        if (activity.length == 0) {
            alert("请选择需要关联的市场活动数据");
            return;
        }

        let activityId = activity[0].value;

        let activityName = $("#n_" + activityId).html();

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
        if (owner == "") {
            alert("所有者不能为空");
            return;
        }

        if (name == "") {
            alert("名称不能为空");
            return;
        }

        if (expectedDate == "") {
            alert("成交日期不能为空");
            return;
        }

        if (customerName == "") {
            alert("客户名称不能为空");
            return;
        }

        if (stage == "") {
            alert("阶段不能为空");
            return;
        }

        //发送请求,新增记录
        post(
            "workbench/transaction/saveTransaction.do?customerName=" + customerName,
            {
                owner: owner,
                name: name,
                expectedDate: expectedDate,
                customerName: customerName,
                stage: stage,
                money: money,
                type: type,
                possibility: possibility,
                source: source,
                activityId: activityId,
                contactsId: contactsId,
                description: description,
                contactSummary: contactSummary,
                nextContactTime: nextContactTime
            }, data => {
                if (checked(data))
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
        boolean flag=transactionService.saveTransaction(tran,customerName,getName(),getTime());

        return ok(flag);
        }
```

* 后台代码 ```service```

```java
@Override
public boolean saveTransaction(Tran tran,String customerName,String name,String time){

        //根据客户名称,查询当前客户信息
        Customer customer=customerDao.findByName(customerName);

        if(ObjectUtils.isEmpty(customer)){
        //新增客户信息
        customer=Customer.builder()
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
        int a=tranDao.insert(tran);

        if(a<=0)
        throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

        //新增交易历史记录
        TranHistory history=TranHistory.builder()
        .id(IdUtils.getId())
        .stage(tran.getStage())
        .money(tran.getMoney())
        .expectedDate(tran.getExpectedDate())
        .createTime(time)
        .createBy(name)
        .tranId(tran.getId())
        .build();

        tranHistoryDao.insert(history);

        return a>0;
        }
```

## 加载交易详情页面基本信息

* 后台代码

```java
@RequestMapping("/toDetail.do")
public String toDetail(@RequestParam("id") String id,Model model,HttpServletRequest request){
        //根据交易id查询交易数据
        Tran tran=transactionService.findTransaction(id);

        if(ObjectUtils.isNotEmpty(tran)){

        model.addAttribute("tran",tran);

        //加载可能性数据
        //根据request对象获取服务器缓存数据
        Map<String, String> sapMap=(Map<String, String>)request.getServletContext().getAttribute("sapMap");

        String stage=tran.getStage();

        if(StringUtils.isNotBlank(stage))
        model.addAttribute("possibility",sapMap.get(stage));
        }

        return"/workbench/transaction/detail";
        }
```

* Sql

```xml

<select id="findById" resultType="com.bjpowernode.crm.workbench.domain.Tran">
    select
    t.*,a.name as activityName,u.name as username,co.fullname as contactsName,cu.name as customerName
    from tbl_tran t
    left join tbl_activity a on t.activityId = a.id
    left join tbl_user u on t.owner = u.id
    left join tbl_contacts co on t.contactsId = co.id
    left join tbl_customer cu on t.customerId = cu.id
    where t.id = #{id}
</select>
```

## 加载交易历史记录

* 前端代码

```javascript
function getTranHistoryList() {
    //发送get方式请求,根据交易id,查询历史列表数据
    get(
        "workbench/transaction/getTranHistoryList.do",
        {
            tranId: $("#tranId").val()
        }, data => {
            if (checked(data))
                return;

            //异步加载
            load(
                $("#tranHistoryListBody"),
                data,
                (i, n) => {
                    return '<tr>' +
                        '<td>' + n.stage + '</td>' +
                        '<td>' + n.money + '</td>' +
                        '<td>' + n.possibility + '</td>' +
                        '<td>' + n.expectedDate + '</td>' +
                        '<td>' + n.createTime + '</td>' +
                        '<td>' + n.createBy + '</td>' +
                        '</tr>';
                }
            )
        }
    )
}
```

* 后台代码

```java
@RequestMapping("/getTranHistoryList.do")
@ResponseBody
public R<List<TranHistory>>getTranHistoryList(@RequestParam("tranId")String tranId,HttpServletRequest request){

        //获取阶段和可能性的map集合
        Map<String, String> sapMap=(Map<String, String>)request.getServletContext().getAttribute("sapMap");

        //查询交易历史记录
        List<TranHistory> tranHistoryList=transactionService.findTransactionHistoryList(tranId);

        if(!CollectionUtils.isEmpty(tranHistoryList)){
        //处理可能性数据
        tranHistoryList.forEach(history->history.setPossibility(sapMap.get(history.getStage())));
        }

        return ok(tranHistoryList);
        }
```

* Sql

```xml

<select id="findList" resultType="com.bjpowernode.crm.workbench.domain.TranHistory">
    select * from tbl_tran_history where tranId = #{tranId}
</select>
```

## 加载阶段图标

```html
<!-- 阶段状态 -->
<div style="position: relative; left: 40px; top: -50px;">
    阶段&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="资质审查" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="需求分析" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="价值建议" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="确定决策者" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-map-marker mystage" data-toggle="popover" data-placement="bottom"
               data-content="提案/报价" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="谈判/复审"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="成交"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="丢失的线索"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="因竞争丢失关闭"></span>--%>
    <%-- -------------%>
    <%
    /*
    图标状态分类:
    1. 交易中的状态
    最后两个图标是黑叉,前七个不固定,已完成图标,当前所处阶段图标,未完成阶段图标
    2. 丢失后的状态
    前七个图标是黑圈,后两个是红叉和黑叉
    */
    //准备加载图标所需的数据
    //阶段和可能性对应的map集合
    Map
    <String
    ,String> sapMap = (Map
    <String
    , String>) application.getAttribute("sapMap");
    //排好序的字典值列表数据,从阶段1到阶段9
    List
    <DictionaryValue> dictionaryValueList = (List
        <DictionaryValue>) application.getAttribute("stage");
            //当前阶段和可能性
            String curStage = ((Tran) request.getAttribute("tran")).getStage();
            String curPossibility = (String) request.getAttribute("possibility");

            //如何判断加载的图标是那种状态呢?
            //根据可能性进行判断,如果可能性为0%,代表是丢失后的交易状态,否则是交易中的状态
            if(StringUtils.equals("0%",curPossibility)){
            //丢失后状态
            //遍历集合,生成9个图标
            for(int i=0; i
            <dictionaryValueList.size
            (); i++){
            //获取当前遍历的阶段和对应可能性,用于判断的
            String stage = dictionaryValueList.get(i).getValue();
            String possibility = sapMap.get(stage);

            //判断生成的图标
            if(StringUtils.equals("0%",possibility)){
            //生成的是最后两个图标
            if(StringUtils.equals(curStage,stage)){
            //红叉
            %>
            <span data-content="<%=stage%>" style="color: #FF0000;" class="glyphicon glyphicon-remove mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }else{
            //黑叉
            %>
            <span data-content="<%=stage%>" style="color: #000000;" class="glyphicon glyphicon-remove mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }
            }else{
            //生成的是前七个图标
            //黑圈
            %>
            <span data-content="<%=stage%>" style="color: #000000;" class="glyphicon glyphicon-record mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }
            }
            }else{
            //交易中状态
            //先获取到当前所处阶段的索引值
            int index = 0;
            //如果遍历的索引值比当前索引值小,证明是已完成的阶段
            //如果遍历的索引值和它相等,证明是当前所处的阶段
            //如果遍历的索引值比当前索引值大,证明是未完成的阶段
            for(int i=0; i
            <dictionaryValueList.size
            (); i++) {
            String stage = dictionaryValueList.get(i).getValue();
            if(StringUtils.equals(stage,curStage)){
            //跳出当前遍历
            index = i;
            break;
            }
            }

            //遍历生成9个图标
            for(int i=0; i
            <dictionaryValueList.size
            (); i++) {
            //获取当前遍历的阶段和对应可能性,用于判断的
            String stage = dictionaryValueList.get(i).getValue();
            String possibility = sapMap.get(stage);

            if(StringUtils.equals("0%",possibility)){
            //黑叉
            %>
            <span data-content="<%=stage%>" style="color: #000000;" class="glyphicon glyphicon-remove mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }else{
            //交易中的前七个图标
            if(i < index){
            //已完成图标
            %>
            <span data-content="<%=stage%>" style="color: #90F790;" class="glyphicon glyphicon-ok-circle mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }else if(i == index){
            //当前所处阶段图标
            %>
            <span data-content="<%=stage%>" style="color: #90F790;" class="glyphicon glyphicon-map-marker mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }else{
            //未完成图标
            %>
            <span data-content="<%=stage%>" style="color: #000000;" class="glyphicon glyphicon-record mystage"
                  data-toggle="popover" data-placement="bottom"></span>
            -----------
            <%
            }
            }
            }
            }
            %>
            <span class="closingDate">${tran.expectedDate}</span>
</div>
```

## 更新阶段及图标

* 页面

```html
<!-- 阶段状态 -->
<div style="position: relative; left: 40px; top: -50px;">
    阶段&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="资质审查" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="需求分析" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="价值建议" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover" data-placement="bottom"
               data-content="确定决策者" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-map-marker mystage" data-toggle="popover" data-placement="bottom"
               data-content="提案/报价" style="color: #90F790;"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="谈判/复审"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="成交"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="丢失的线索"></span>--%>
    <%-- -------------%>
    <%-- <span class="glyphicon glyphicon-record mystage" data-toggle="popover" data-placement="bottom"
               data-content="因竞争丢失关闭"></span>--%>
    <%-- -------------%>
    <%
    /*
    图标状态分类:
    1. 交易中的状态
    最后两个图标是黑叉,前七个不固定,已完成图标,当前所处阶段图标,未完成阶段图标
    2. 丢失后的状态
    前七个图标是黑圈,后两个是红叉和黑叉
    */
    //准备加载图标所需的数据
    //阶段和可能性对应的map集合
    Map
    <String
    ,String> sapMap = (Map
    <String
    , String>) application.getAttribute("sapMap");
    //排好序的字典值列表数据,从阶段1到阶段9
    List
    <DictionaryValue> dictionaryValueList = (List
        <DictionaryValue>) application.getAttribute("stage");
            //当前阶段和可能性
            String curStage = ((Tran) request.getAttribute("tran")).getStage();
            String money = ((Tran) request.getAttribute("tran")).getMoney();
            String expectedDate = ((Tran) request.getAttribute("tran")).getExpectedDate();
            String curPossibility = (String) request.getAttribute("possibility");

            //如何判断加载的图标是那种状态呢?
            //根据可能性进行判断,如果可能性为0%,代表是丢失后的交易状态,否则是交易中的状态
            if(StringUtils.equals("0%",curPossibility)){
            //丢失后状态
            //遍历集合,生成9个图标
            for(int i=0; i
            <dictionaryValueList.size
            (); i++){
            //获取当前遍历的阶段和对应可能性,用于判断的
            String stage = dictionaryValueList.get(i).getValue();
            String possibility = sapMap.get(stage);

            //判断生成的图标
            if(StringUtils.equals("0%",possibility)){
            //生成的是最后两个图标
            if(StringUtils.equals(curStage,stage)){
            //红叉
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #FF0000;" class="glyphicon glyphicon-remove mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }else{
            //黑叉
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #000000;" class="glyphicon glyphicon-remove mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }
            }else{
            //生成的是前七个图标
            //黑圈
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #000000;" class="glyphicon glyphicon-record mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }
            }
            }else{
            //交易中状态
            //先获取到当前所处阶段的索引值
            int index = 0;
            //如果遍历的索引值比当前索引值小,证明是已完成的阶段
            //如果遍历的索引值和它相等,证明是当前所处的阶段
            //如果遍历的索引值比当前索引值大,证明是未完成的阶段
            for(int i=0; i
            <dictionaryValueList.size
            (); i++) {
            String stage = dictionaryValueList.get(i).getValue();
            if(StringUtils.equals(stage,curStage)){
            //跳出当前遍历
            index = i;
            break;
            }
            }

            //遍历生成9个图标
            for(int i=0; i
            <dictionaryValueList.size
            (); i++) {
            //获取当前遍历的阶段和对应可能性,用于判断的
            String stage = dictionaryValueList.get(i).getValue();
            String possibility = sapMap.get(stage);

            if(StringUtils.equals("0%",possibility)){
            //黑叉
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #000000;" class="glyphicon glyphicon-remove mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }else{
            //交易中的前七个图标
            if(i < index){
            //已完成图标
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #90F790;" class="glyphicon glyphicon-ok-circle mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }else if(i == index){
            //当前所处阶段图标
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #90F790;" class="glyphicon glyphicon-map-marker mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }else{
            //未完成图标
            %>
            <span onclick="updateStage('<%=stage%>','<%=money%>','<%=expectedDate%>')" data-content="<%=stage%>"
                  style="color: #000000;" class="glyphicon glyphicon-record mystage" data-toggle="popover"
                  data-placement="bottom"></span>
            -----------
            <%
            }
            }
            }
            }
            %>
            <span class="closingDate">${tran.expectedDate}</span>
</div>
```

* 前端代码

```javascript
function updateStage(stage, money, expectedDate) {
    //alert(stage + " "+ money + " "+expectedDate)
    let tranId = $("#tranId").val();

    //根据交易id,更新交易的阶段及新增交易历史记录
    //更新成功后,刷新当前页面
    get(
        "workbench/transaction/updateStage.do",
        {
            stage: stage,
            money: money,
            expectedDate: expectedDate,
            tranId: tranId,
        }, data => {
            if (checked(data)) return;

            to("workbench/transaction/toDetail.do?id=" + tranId);
        }
    )
}
```

* 后台代码 `controller`

```java
@RequestMapping("/updateStage.do")
@ResponseBody
public R updateStage(@RequestParam("tranId")String tranId,
@RequestParam("stage")String stage,
@RequestParam("money")String money,
@RequestParam("expectedDate")String expectedDate){
        checked(
        tranId,stage,money,expectedDate
        );

        //更新和新增操作
        transactionService.updateStage(tranId,money,expectedDate,getName(),getTime(),stage);

        return ok();
        }
```

* 后台代码 `service`

```java
@Override
public void updateStage(String tranId,String money,String expectedDate,String name,String time,String stage){
        //更新交易阶段
        int a=tranDao.updateStage(
        tranId,
        stage,
        name,
        time
        );

        if(a<=0)
        throw new RuntimeException(State.DB_UPDATE_ERROR.getMsg());

        //新增交易历史记录
        tranHistoryDao.insert(
        TranHistory.builder()
        .id(IdUtils.getId())
        .stage(stage)
        .money(money)
        .expectedDate(expectedDate)
        .createTime(time)
        .createBy(name)
        .tranId(tranId)
        .build()
        );
        }
```

* Sql

```java
@Update("update tbl_tran set stage=#{stage},editBy=#{name},editTime=#{time} where id = #{tranId}")
int updateStage(@Param("tranId") String tranId,@Param("stage") String stage,@Param("name") String name,@Param("time") String time);
```

## Echarts-统计图表-入门案例

* 柱状图

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>入门案例</title>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/ECharts/echarts.min.js"></script>
    <script type="text/javascript" src="js/ajax.js"></script>

</head>
<body>

<%--引入标签容器--%>
<div id="main" style="width: 600px;height:400px;"></div>

<script>
    var myChart = echarts.init(document.getElementById('main'));

    var option = {
        title: {
            //标题内容
            text: 'ECharts 入门示例'
        },
        tooltip: {},
        legend: {
            //分类
            data: ['销量']
        },
        xAxis: {
            //X轴的数据,每个不同的分类
            data: ["衬衫", "羊毛衫", "雪纺衫", "裤子", "高跟鞋", "袜子"]
        },
        yAxis: {},
        series: [{
            //对应展示的分类选项
            name: '销量',
            //图标类型,柱状图
            type: 'bar',
            //每个x轴所对应展示的数据值
            data: [5, 20, 36, 10, 10, 20]
        }]
    };

    myChart.setOption(option);
</script>
</body>
</html>
```

* 折线图

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>入门案例</title>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/ECharts/echarts.min.js"></script>
    <script type="text/javascript" src="js/ajax.js"></script>

</head>
<body>

<%--引入标签容器--%>
<div id="main" style="width: 600px;height:400px;"></div>

<script>
    var myChart = echarts.init(document.getElementById('main'));

    var option = {
        title: {
            //标题内容
            text: 'ECharts 入门示例'
        },
        tooltip: {},
        legend: {
            //分类
            data: ['销量']
        },
        xAxis: {
            //X轴的数据,每个不同的分类
            data: ["衬衫", "羊毛衫", "雪纺衫", "裤子", "高跟鞋", "袜子"]
        },
        yAxis: {},
        series: [{
            //对应展示的分类选项
            name: '销量',
            //图标类型,折线图
            type: 'line',
            //每个x轴所对应展示的数据值
            data: [5, 20, 36, 10, 10, 20]
        }]
    };

    myChart.setOption(option);
</script>
</body>
</html>
```

* 饼状图

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>入门案例</title>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/ECharts/echarts.min.js"></script>
    <script type="text/javascript" src="js/ajax.js"></script>

</head>
<body>

<%--引入标签容器--%>
<div id="main" style="width: 600px;height:400px;"></div>

<script>
    var myChart = echarts.init(document.getElementById('main'));

    var option = {
        title: {
            text: 'Referer of a Website',
            subtext: 'Fake Data',
            left: 'center'
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: 'left'
        },
        series: [
            {
                name: 'Access From',
                type: 'pie',
                radius: '50%',
                data: [
                    {value: 1048, name: 'Search Engine'},
                    {value: 735, name: 'Direct'},
                    {value: 580, name: 'Email'},
                    {value: 484, name: 'Union Ads'},
                    {value: 300, name: 'Video Ads'}
                ],
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    };

    myChart.setOption(option);
</script>
</body>
</html>
```

* 漏斗图

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>入门案例</title>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/ECharts/echarts.min.js"></script>
    <script type="text/javascript" src="js/ajax.js"></script>

</head>
<body>

<%--引入标签容器--%>
<div id="main" style="width: 1200px;height:800px;"></div>

<script>
    var myChart = echarts.init(document.getElementById('main'));

    var option = {
        title: {
            text: 'Funnel'
        },
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b} : {c}%'
        },
        toolbox: {
            feature: {
                dataView: {readOnly: false},
                restore: {},
                saveAsImage: {}
            }
        },
        legend: {
            data: ['Show', 'Click', 'Visit', 'Inquiry', 'Order']
        },
        series: [
            {
                name: 'Funnel',
                type: 'funnel',
                left: '10%',
                top: 60,
                bottom: 60,
                width: '80%',
                min: 0,
                max: 100,
                minSize: '0%',
                maxSize: '100%',
                sort: 'descending',
                gap: 2,
                label: {
                    show: true,
                    position: 'inside'
                },
                labelLine: {
                    length: 10,
                    lineStyle: {
                        width: 1,
                        type: 'solid'
                    }
                },
                itemStyle: {
                    borderColor: '#fff',
                    borderWidth: 1
                },
                emphasis: {
                    label: {
                        fontSize: 20
                    }
                },
                 data: [
                     { value: 60, name: 'Visit' },
                     { value: 40, name: 'Inquiry' },
                     { value: 20, name: 'Order' },
                     { value: 80, name: 'Click' },
                     { value: 100, name: 'Show' }
                 ]
            }
        ]
    };

    myChart.setOption(option);
</script>
</body>
</html>
```

## 加载交易阶段统计图表-漏斗图
* 页面
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>入门案例</title>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/ECharts/echarts.min.js"></script>
    <script type="text/javascript" src="js/ajax.js"></script>

</head>
<body>

<%--引入标签容器--%>
<div id="main" style="width: 1200px;height:800px;"></div>

<script>

    $(function () {
        //1. 加载交易阶段数量-漏斗图
        var myChart = echarts.init(document.getElementById('main'));

        get(
            "workbench/chart/transaction/getTranStageData.do",
            {},
            data=>{
                //data:{code:xxx,msg:xxx,success:xxx,data:{nameList:[xxx...],dataList:[{name:xxx,value:xxx}...]}}
                if(checked(data)) return;

                var option = {
                    title: {
                        text: 'Funnel'
                    },
                    tooltip: {
                        trigger: 'item',
                        formatter: '{a} <br/>{b} : {c}%'
                    },
                    toolbox: {
                        feature: {
                            dataView: { readOnly: false },
                            restore: {},
                            saveAsImage: {}
                        }
                    },
                    legend: {
                        //data: ['Show', 'Click', 'Visit', 'Inquiry', 'Order']
                        data: data.data.nameList
                    },
                    series: [
                        {
                            name: 'Funnel',
                            type: 'funnel',
                            left: '10%',
                            top: 60,
                            bottom: 60,
                            width: '80%',
                            min: 0,
                            max: 100,
                            minSize: '0%',
                            maxSize: '100%',
                            sort: 'descending',
                            gap: 2,
                            label: {
                                show: true,
                                position: 'inside'
                            },
                            labelLine: {
                                length: 10,
                                lineStyle: {
                                    width: 1,
                                    type: 'solid'
                                }
                            },
                            itemStyle: {
                                borderColor: '#fff',
                                borderWidth: 1
                            },
                            emphasis: {
                                label: {
                                    fontSize: 20
                                }
                            },
                            // data: [
                            //     { value: 60, name: 'Visit' },
                            //     { value: 40, name: 'Inquiry' },
                            //     { value: 20, name: 'Order' },
                            //     { value: 80, name: 'Click' },
                            //     { value: 100, name: 'Show' }
                            // ]
                            data: data.data.dataList
                        }
                    ]
                };

                myChart.setOption(option);
            }
        )
    })
</script>
</body>
</html>
```

* 后台代码
```java
@RequestMapping("/transaction/getTranStageData.do")
@ResponseBody
public R getTranStageData(){
    //查询每个交易阶段的数量,这里我们查询的是交易历史表,因为交易表没有这么多的记录
    //每个map集合是{name:xxx,value:xxx}
    List<Map<String,Object>> dataList = transactionService.getChartDate();

    //将集合中的数据提取出来,组装成nameList
    List<Object> nameList = dataList.stream()
            //提取数据,map参数,代表遍历的每个map集合
            //将map集合中的name属性值,提取出来
            //将提取出来的数据,收集成一个List集合并返回
            .map(map -> map.get("name")).collect(Collectors.toList());

    //组装返回值结果集
    Map<String,List> resultMap = new HashMap<>();
    resultMap.put("nameList",nameList);
    resultMap.put("dataList",dataList);

    return ok(resultMap);
}
```

* Sql
```java
@Select("select stage as name,count(id) as value from tbl_tran_history group by stage")
List<Map<String, Object>> findChartData();
```