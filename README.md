# Crm项目笔记

## 加载字典值列表 `异步加载`
* 前端代码
```javascript
function getDictionaryValueList() {
    get(
        "settings/dictionary/value/getDictionaryValueList.do",
        {},
        data=>{
            if(checked(data))
                return;

            //异步加载
            /*
                最终将数据异步加载到页面的标签对象中

                页面中只提供标签对象

                在js代码中以字符串的方式来封装加载的标签数据,最终填充到标签对象中
             */
            //1. 定义字符串,用于封装标签数据
            let html = "";

            //2. 遍历集合数据,将标签的模板加载为动态的数据
            $.each(data.data,function (i, n) {
                html += '<tr class="'+(i%2==0?'active':'')+'">';
                html += '<td><input type="checkbox"/></td>';
                html += '<td>'+(i+1)+'</td>';
                html += '<td>'+n.value+'</td>';
                html += '<td>'+n.text+'</td>';
                html += '<td>'+n.orderNo+'</td>';
                html += '<td>'+n.typeCode+'</td>';
                html += '</tr>';
            })

            //3. 将html字符串,加载到标签容器中
            $("#dictionaryValueListBody").html(html);
        }
    )
}
```

* 后台代码
```java
@RequestMapping("/value/getDictionaryValueList.do")
@ResponseBody
public R getDictionaryValueList(){
    //查询所有的数据字典值的列表
    List<DictionaryValue> dictionaryValueList = dictionaryService.findDictionaryValueList();

    return okAndCheck(dictionaryValueList);
}
```


## 抽取异步加载的方法
* 前端代码
```javascript
function load(jqueryObj,data,callBack){
    //1. 定义字符串,用于封装标签数据
    let html = "";

    //2. 遍历集合数据,将标签的模板加载为动态的数据
    $.each(data.data,function (i,n) {
        html += callBack(i,n);
    })

    //3. 将html字符串,加载到标签容器中
    $(jqueryObj).html(html);
}
```


## 全选和反选操作 `事件传递`
* 前端代码
```javascript
function selectValueAll() {
    $("#selectAllBtn").click(function (){
        //获取所有复选框的标签对象
        $("input[name=flag]").prop("checked",this.checked);
    })
}


function reverseValueAll() {
    //按照之前的方式来实现反选操作,结论:这种方式无法直接给异步加载的标签对象绑定事件
    //注意之前的方式是,全选框和复选框的加载都在jsp页面中
    //$("input[name=flag]").click(function () {
    //    alert("123")
    //})

    //现在的方式,全选框在jsp页面中加载的,复选框在js文件中加载的
    //如何给异步加载的标签对象绑定事件,要通过页面中的父标签向子标签来传递事件
    //通过父标签来给子标签绑定事件
    //参数1,传递的事件名称
    //参数2,传递的jquery对象
    //参数3,回调方法
    $("#dictionaryValueListBody").on("click","input[name=flag]",function () {
        $("#selectAllBtn").prop(
            "checked",
            $("input[name=flag]").length == $("input[name=flag]:checked").length
        )
    })
}
```


## 新增字典值操作
### 加载字典类型列表数据
* 前端代码
```javascript
function getDictionaryTypeList() {
    get(
        "settings/dictionary/type/getDictionaryTypeList.do",
        {},
        data=>{
            if(checked(data)) return;

            //异步加载操作
            loadHtml(
                $("#create-typeCode"),
                data,
                function (i, n) {
                    return "<option value="+n.code+">"+n.name+"</option>"
                },
                "<option></option>"
            )
        }
    )
}
```

* 后台代码
```java
@RequestMapping("/type/getDictionaryTypeList.do")
@ResponseBody
public R getDictionaryTypeList(){
    //查询字典类型列表数据
    List<DictionaryType> dictionaryTypeList = dictionaryService.findDictionaryTypeList();

    return okAndCheck(dictionaryTypeList);
}
```

### 新增操作
* 前端代码
```javascript
function saveDictionaryValue() {
    $("#saveDictionaryValueBtn").click(function () {
        //获取属性信息
        let text = $("#create-text").val();
        let value = $("#create-value").val();
        let orderNo = $("#create-orderNo").val();
        let typeCode = $("#create-typeCode").val();

        if(typeCode == ""){
            alert("字典类型编码不能为空");
            return;
        }

        if(value == ""){
            alert("字典值不能为空");
            return;
        }

        //校验通过
        post(
            "settings/dictionary/value/saveDictionaryValue.do",
            {
                text:text,
                value:value,
                orderNo:orderNo,
                typeCode:typeCode,
            },data=>{
                if(checked(data)) return;

                //新增成功,跳转到字典值首页面
                to("settings/dictionary/value/toIndex.do")
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/value/saveDictionaryValue.do")
@ResponseBody
public R saveDictionaryValue(@RequestBody DictionaryValue dictionaryValue){
    //校验
    checked(
            dictionaryValue.getTypeCode(),
            dictionaryValue.getValue()
    );

    return ok(
            //新增之前需要先进行赋值操作,id,可以通过UUID的方式来新增
            dictionaryService.saveDictionaryValue(
                    dictionaryValue.setId(IdUtils.getId())
            ),
            State.DB_SAVE_ERROR
    );
}
```

## 字典值模块-作业
> 业务逻辑请参考,字典类型的修改和删除操作
> 字典值的删除操作,无需考虑一对多关系,因为我们删除的就是多方数据,可以直接删除
* 字典值修改操作
* 字典值的删除操作


## 市场活动模块介绍
> 公司中举办的活动,我们在该模块中全部都能查询得到
> 市场活动模块和市场活动详情模块及线索和联系人模块都有相应的关联

## 市场活动表关系
* tbl_activity
    * id `主键`
    * owner `外键,用户表`
    * name `活动名称`
    * startDate `开始日期`
    * endDate `结束日期`
    * cost `成本`
    * description `描述`
    * createTime `创建时间`
    * createBy `创建人`
    * editTime `修改时间`
    * editBy `修改人`
    * isDelete `逻辑删除标记`