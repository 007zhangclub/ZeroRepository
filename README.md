# Crm项目笔记
## 新增交易操作
### 回显所有者下拉列表数据
* 后台代码
```java
@RequestMapping("/toSave.do")
public String toSave(Model model){

    //查询所有的用户列表数据
    List<User> userList = userService.findUserList();

    if(!CollectionUtils.isEmpty(userList))
        model.addAttribute("userList",userList);

    return "/workbench/transaction/save";
}
```

* 页面
```html
<select class="form-control" id="create-owner">
    <c:forEach items="${userList}" var="u">
        <option value="${u.id}" ${user.id == u.id ? 'selected' : ''}>${u.name}</option>
    </c:forEach>
</select>
```
  
### 自动补全客户名称
* 前端代码
```javascript
function autoLoadCustomerName() {
    //给输入框对象绑定自动补全事件
    $("#create-customerName").typeahead({
        //query代表输入的关键字
        //process代表将返回值结果进行解析,回显到输入框中
        source:function (query,process) {
            get(
                "workbench/transaction/getCustomerName.do",
                {customerName:query},
                data=>{
                    if(checked(data)) return;

                    //返回值的数据是List<String>,客户名称的列表数据
                    process(data.data);
                }
            )
        },
        //当输入内容时,每隔多久发送一次请求
        delay:500
    })
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

* Sql
```xml
@Select("select name from tbl_customer where name like '%' #{customerName} '%'")
List<String> getCustomerNameList(String customerName);
```