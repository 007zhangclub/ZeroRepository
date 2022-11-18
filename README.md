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

## 根据阶段加载可能性数据
* 监听器
```java
/*
    让当前的类实现ServletContextListener接口
        重写两个方法
            初始化方法 -> 服务器在运行时,我们的listener即被调用,向ServletContext中存入数据
                        当服务器启动完成时,我们的数据已经被加载到ServletContext域对象中
            结束时方法
 */
@Slf4j
public class LoadCacheDataListener implements ServletContextListener {

    //@Autowired
    //private DictionaryService dictionaryService;

    /*
    服务器初始化运行的回调方法
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("加载缓存数据...");

        //将容器中的DictionaryService对象获取到
        //如果通过自动注入的方式,是获取不到的,因为服务器在刚开始运行的时候,我们的容器并没有加载完成
        //所以此时获取到的业务层对象是空
        //我们要通过手动加载容器的方式来后去业务层对象
        ApplicationContext app = new ClassPathXmlApplicationContext("spring/applicationContext-service.xml");

        //从容器中获取service对象
        DictionaryService dictionaryService = app.getBean(DictionaryService.class);

        Map<String, List<DictionaryValue>> cacheData = dictionaryService.findCacheData();

        //遍历集合数据,将它存入到ServletContext域对象中
        cacheData.forEach(
                (k, v) -> sce.getServletContext().setAttribute(k, v)
        );

        //log.info("DictionaryService : {}",dictionaryService);

        //加载阶段和可能性的数据到服务器缓存中
        //指定的属性名称,不要携带后缀名
        ResourceBundle bundle = ResourceBundle.getBundle("properties/Stage2Possibility");

        //创建一个Map集合,用于封装阶段和可能性对应的数据
        Map<String, String> sapMap = new HashMap<>();

        bundle.keySet().forEach(key -> sapMap.put(key, bundle.getString(key)));

        //存入到缓存中
        //if(!CollectionUtils.isEmpty(sapMap))
        sce.getServletContext().setAttribute("sapMap", sapMap);

        System.out.println("sapMap : "+sapMap);
        System.out.println("缓存数据加载完成...");
    }
}
```

* 前端代码
```javascript
//这个转换的方式由于用到了java+js的方式,所以不能够放在js文件中执行
function loadStageAndPossibility2Json() {
    return {
        <%
            //从服务器缓存中获取到sapMap集合数据
            Map<String,String> sapMap = (Map<String, String>) application.getAttribute("sapMap");

            //遍历map集合,这里不要使用foreach来遍历,stream api在jsp中是不支持的
            //将java分隔开,使用js代码进行加载,拼接成json数据
            for (Map.Entry<String,String> entry : sapMap.entrySet()) {
                //获取每个entry对象的key和value,用于拼接json中的key和value
                String key = entry.getKey();
                String value = entry.getValue();
        %>
                "<%=key%>": "<%=value%>",
        <%
            }
        %>
    };
}

function loadPossibilityByStage(json) {
    //给下拉框绑定事件,当选中了其他的选项时,根据当前的选项的名称,加载对应的可能性数据到只读的输入框中
    $("#create-stage").change(function () {
        //获取当前选中的选项数据(阶段名称)
        let stage = $(this).val();

        //根据阶段名称加载可能性数据
        let possibility = json[stage];

        $("#create-possibility").val(possibility);
    })
}
```

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