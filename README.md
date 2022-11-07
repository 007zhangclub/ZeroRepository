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