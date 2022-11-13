# Crm项目笔记
## 新增市场活动备注信息
* 前端代码
```javascript
function saveActivityRemark() {
    $("#saveActivityRemarkBtn").click(function () {
        //获取文本域中的内容
        let noteContent = $("#remark").val();

        if(noteContent == ""){
            alert("备注信息不能为空");
            return;
        }

        //新增的是多方的数据,必须携带市场活动的id
        let activityId = $("#activityId").val();

        post(
            "workbench/activity/remark/saveActivityRemark.do",
            {
                activityId:activityId,
                noteContent:noteContent
            },data=>{
                if(checked(data))
                    return;

                //刷新列表数据
                getActivityRemarkList();

                //清空文本域中的备注信息内容
                $("#remark").val("");
            }
        )

    })
}
```

* 后台代码
```java
@RequestMapping("/remark/saveActivityRemark.do")
@ResponseBody
public R saveActivityRemark(@RequestBody ActivityRemark activityRemark){
    //赋值操作
    activityRemark.setId(IdUtils.getId())
            .setEditFlag("0")
            .setCreateTime(getTime())
            .setCreateBy(getName())
            .setEditTime(getTime())
            .setEditBy(getName());

    //新增操作
    boolean flag = activityService.saveActivityRemark(activityRemark);

    return ok(flag,State.DB_SAVE_ERROR);
}
```

## 修改市场活动备注信息
* 前端代码
```javascript
/*
    由于当前的页面的模态窗口中,只有一个文本域,数据较少,我们可以从页面中获取,然后回显

    在js异步加载时,我们传递的参数必须由4个单引号进行嵌套
        首位两个单引号进行转义即可
 */
function openEditRemarkModal(id, noteContent) {
    //将备注信息的id,存入到隐藏域中,为后续的修改操作做铺垫
    $("#edit-remarkId").val(id);

    //在模态窗口中显示备注信息
    $("#noteContent").val(noteContent);

    //打开模态窗口
    $("#editRemarkModal").modal("show");
}


function updateActivityRemark() {
    $("#updateRemarkBtn").click(function () {
        //获取隐藏域中的id
        let remarkId = $("#edit-remarkId").val();

        //获取文本域中的内容
        let noteContent = $("#noteContent").val();

        if(noteContent == ""){
            alert("备注信息不能为空")
            return;
        }

        //获取页面中的备注信息
        let oldContent = $("#n_"+remarkId).html();

        if(noteContent == oldContent){
            alert("修改信息不能与原数据一致")
            return;
        }

        //发送post请求修改操作
        post(
            "workbench/activity/remark/updateActivityRemark.do",
            {
                id:remarkId,
                noteContent:noteContent
            },data=>{
                if(checked(data))
                    return;

                //刷新列表数据
                getActivityRemarkList()

                //关闭模态窗口
                $("#editRemarkModal").modal("hide");
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/remark/updateActivityRemark.do")
@ResponseBody
public R updateActivityRemark(@RequestBody ActivityRemark activityRemark){
    //赋值操作
    activityRemark.setEditFlag("1")
            .setEditBy(getName())
            .setEditTime(getTime());

    //修改操作
    boolean flag = activityService.updateActivityRemark(activityRemark);

    return ok(flag,State.DB_UPDATE_ERROR);
}
```

## 删除市场活动备注信息
* 前端代码
```javascript
function deleteActivityRemark(remarkId) {
    //删除是一个危险的动作,需要给出提示信息
    if(confirm("确定要删除吗?"))
        get(
            "workbench/activity/remark/deleteActivityRemark.do",
            {remarkId:remarkId},
            data=>{
                if(checked(data))
                    return;
                //删除成功后,刷新列表页面
                getActivityRemarkList();
            }
        )
}
```

* 后台代码
```java
@RequestMapping("/remark/deleteActivityRemark.do")
@ResponseBody
public R deleteActivityRemark(@RequestParam("remarkId") String remarkId){
    return ok(
            activityService.deleteActivityRemark(remarkId),
            State.DB_DELETE_ERROR
    );
}
```


## 缓存介绍
> 缓存是将数据存入到内存中,我们在加载时,无需通过数据库(磁盘)进行序列化和反序列化的过程,性能和效率提升了非常多
> 加载数据的顺序: CPU缓存 -> 内存 -> 磁盘 -> 云盘
* 什么数据适合存储到缓存中呢?
    * 早期的时候,由于服务器的性能没有那么高,我们会将一些几乎不怎么变化的数据,存入到缓存中
    * 现在的时候,我们的计算机或者服务器的性能已经有了很显著的提升,我们就无需担心这些数据了
* 现在我们还没有学习到一些第三方的缓存技术,我们通过服务器缓存来实现
    * 将数据存储到ServletContext域对象中,这样我们全局都可以获取到该数据了
    * 获取缓存数据时,就是通过el表达式来进行加载
## Crm中缓存中存储的数据
* 将数据字典类型(一方)和数据字典值(多方)的内容存入到缓存中
    * 因为页面中我们的下拉框会加载这些较固定的数据
* `[{code:valueList}...]` 将查询出的集合数据,遍历存入到服务器缓存中 `{code:valueList}`

## 加载数据到服务器缓存中
* web.xml
```xml
<listener>
    <listener-class>com.bjpowernode.crm.listener.LoadCacheDataListener</listener-class>
</listener>
```

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

        //获取缓存的数据
        // [
        //  {code:[{DictionaryValue}...]},
        //  {code:[{DictionaryValue}...]},
        //  ...
        // ]
        //List<Map<String,List<DictionaryValue>>> cacheData = dictionaryService.findCacheData();

        //也可以简化存储的数据结构
        /*
            {
                code1:[{DictionaryValue}...],
                code2:[{DictionaryValue}...],
                ...
            }
         */
        Map<String,List<DictionaryValue>> cacheData = dictionaryService.findCacheData();

        //遍历集合数据,将它存入到ServletContext域对象中
        cacheData.forEach(
                (k,v) -> sce.getServletContext().setAttribute(k,v)
        );

        log.info("DictionaryService : {}",dictionaryService);
        System.out.println("缓存数据加载完成...");
    }
}
```

* 后台代码 `service`
```java
@Override
public Map<String, List<DictionaryValue>> findCacheData() {
    //缓存数据
    Map<String, List<DictionaryValue>> resultMap = new HashMap<>();

    //方式1,普通方式获取
    //查询出所有的字典类型编码数据,并进行遍历操作
    dictionaryTypeDao.findAll().forEach(
            dictionaryType -> {
                //获取字典类型编码,根据它来查询多方的数据列表
                String code = dictionaryType.getCode();

                List<DictionaryValue> dictionaryValueList = dictionaryValueDao.findList(code);
                System.out.println("key : "+code);
                System.out.println("dictionaryValueList : "+dictionaryValueList);
                if(!CollectionUtils.isEmpty(dictionaryValueList))
                    resultMap.put(code,dictionaryValueList);
            }
    );

    //方式2(了解),通过stream api来进行集合的分组,获取对应的数据
    //我们可以通过查看的方式,能够知道DictionaryValue对象中就已经包含了我们的编码数据
    //所以我们可以通过查询所有的字典值的列表数据,然后通过stream api来处理这些数据
    //通过我们根据编码来进行分组,得到我们想要的结果
    //Map<String, List<DictionaryValue>> resultMap = dictionaryValueDao.findAll().stream().collect(Collectors.groupingBy(DictionaryValue::getTypeCode));
    //System.out.println(resultMap);

    //封装
    return resultMap;
}
```

* 页面中的使用方式
```html
<div class="input-group-addon">线索状态</div>
<select class="form-control">
  <option></option>
    <c:forEach items="${clueState}" var="cc">
        <option value="${cc.value}">${cc.text}</option>
    </c:forEach>
</select>
```

## 线索模块
> 线索模块是在市场活动中结实的一些有意向的客户信息,最终通过这些客户信息促成一个线索,该线索最终我们想要将客户促成交易
> 也就是说在举办的活动中,结实一些有意向的客户,这些就是我们的线索,后续通过沟通和联系,将这些客户促成交易

## 线索模块表结构
* tbl_clue
  * id
  * fullname `客户名称,也是联系人表名称`
  * appellation `称呼`
  * owner `外键`
  * company `公司名称,也是客户表名称`
  * job `只为`
  * email `邮箱地址`
  * phone `公司联系方式`
  * website `网站`
  * mphone `个人联系方式`
  * state `线索状态`
  * source `线索来源`
  * createBy
  * createTime
  * editBy
  * editTime
  * description `描述信息`
  * contactSummary `联系纪要`
  * nextContactTime `下次联系时间`
  * address `地址`
  
## 线索模块作业
  * 线索分页查询
  * 线索分页组件集成
  * 线索条件过滤查询
  * 线索修改
  * 线索删除

## 新增线索操作
* 前端代码
```javascript
function saveClue() {
    $("#saveClueBtn").click(function () {

        //获取属性信息
        let owner = $("#create-owner").val();
        let company = $("#create-company").val();
        let fullname = $("#create-fullname").val();
        let appellation = $("#create-appellation").val();
        let job = $("#create-job").val();
        let email = $("#create-email").val();
        let phone = $("#create-phone").val();
        let website = $("#create-website").val();
        let mphone = $("#create-mphone").val();
        let state = $("#create-state").val();
        let source = $("#create-source").val();
        let description = $("#create-description").val();
        let contactSummary = $("#create-contactSummary").val();
        let nextContactTime = $("#create-nextContactTime").val();
        let address = $("#create-address").val();

        //校验必传的参数
        if(owner == ""){
            alert("所有者不能为空");
            return;
        }

        if(company == ""){
            alert("公司名称不能为空");
            return;
        }

        if(fullname == ""){
            alert("姓名不能为空");
            return;
        }

        //校验通过后,发送post请求,新增线索
        post(
            "workbench/clue/saveClue.do",
            {
                owner:owner,
                company:company,
                fullname:fullname,
                appellation:appellation,
                job:job,
                email:email,
                phone:phone,
                website:website,
                mphone:mphone,
                state:state,
                source:source,
                description:description,
                contactSummary:contactSummary,
                nextContactTime:nextContactTime,
                address:address,
            },data=>{
                if(checked(data))
                    return;
                //关闭模态窗口,刷新列表数据(作业)
                $("#createClueModal").modal("hide");
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/saveClue.do")
@ResponseBody
public R saveClue(@RequestBody Clue clue){
    //校验必传的参数信息
    checked(
            clue.getOwner(),
            clue.getFullname(),
            clue.getCompany()
    );

    //赋值操作
    clue.setId(IdUtils.getId())
            .setCreateBy(getName())
            .setCreateTime(getTime())
            .setEditBy(getName())
            .setEditTime(getTime());

    //新增操作
    return ok(
            clueService.saveClue(clue),
            State.DB_SAVE_ERROR
    );
}
```

* Sql
```xml
<insert id="insert">
    insert into tbl_clue
    (id,
     fullname,
     appellation,
     owner,
     company,
     job,
     email,
     phone,
     website,
     mphone,
     state,
     source,
     createBy,
     createTime,
     editBy,
     editTime,
     description,
     contactSummary,
     nextContactTime,
     address)
    values (#{id},
            #{fullname},
            #{appellation},
            #{owner},
            #{company},
            #{job},
            #{email},
            #{phone},
            #{website},
            #{mphone},
            #{state},
            #{source},
            #{createBy},
            #{createTime},
            #{editBy},
            #{editTime},
            #{description},
            #{contactSummary},
            #{nextContactTime},
            #{address})
</insert>
```