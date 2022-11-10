# Crm项目笔记

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
* 前端代码
```javascript
function updateActivity() {
    $("#updateActivityBtn").click(function () {
        let id = $("#edit-id").val();
        let owner = $("#edit-owner").val();
        let name = $("#edit-name").val();
        let startDate = $("#edit-startDate").val();
        let endDate = $("#edit-endDate").val();
        let cost = $("#edit-cost").val();
        let description = $("#edit-description").val();

        if(id == ""){
            alert("页面加载异常,请刷新后再试");
            return;
        }

        if(name == ""){
            alert("活动名称不能为空");
            return;
        }

        if(owner == ""){
            alert("所有者不能为空");
            return;
        }

        //发送post请求修改操作
        post(
            "workbench/activity/updateActivity.do",
            {
                id:id,
                owner:owner,
                name:name,
                startDate:startDate,
                endDate:endDate,
                cost:cost,
                description:description,
            },data=>{
                if(checked(data))
                    return;

                //修改成功,刷新列表,关闭模态窗口
                getActivityListPage(1,5);

                $("#editActivityModal").modal("hide");
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/updateActivity.do")
@ResponseBody
public R updateActivity(@RequestBody Activity activity){
    //修改之前,先行赋值
    activity.setEditTime(getTime())
            .setEditBy(getName());

    return ok(
            //修改操作
            activityService.updateActivity(activity),
            State.DB_UPDATE_ERROR
    );
}
```

* sql
```xml
<update id="update">
    update tbl_activity
    <set>
        <if test="owner != null and owner != ''">
            owner = #{owner} ,
        </if>
        <if test="name != null and name != ''">
            name = #{name} ,
        </if>
        <if test="startDate != null and startDate != ''">
            startDate = #{startDate} ,
        </if>
        <if test="endDate != null and endDate != ''">
            endDate = #{endDate} ,
        </if>
        <if test="cost != null and cost != ''">
            cost = #{cost} ,
        </if>
        <if test="description != null and description != ''">
            description = #{description} ,
        </if>
        <if test="editBy != null and editBy != ''">
            editBy = #{editBy} ,
        </if>
        <if test="editTime != null and editTime != ''">
            editTime = #{editTime} ,
        </if>
    </set>
        where id = #{id}
</update>
```


## 删除的分类
1. 物理删除
    * 真实的将数据从数据库中删除掉
    * 删除后的数据无法恢复
2. 逻辑删除
    * 通过update语句,设置字段的值,达到将数据显示或隐藏的目的
    * 删除后的数据可以恢复
    * 逻辑删除操作(删除操作)需要和新增和查询一起联动才可以
        * 新增数据时,isDelete为0,代表显示的数据
        * 查询数据时,需要携带条件isDelete=0
        * 删除数据时,需要将isDelete从0更改为1

## 删除市场活动
* 前端代码
```javascript
function batchDelete() {
    $("#batchDeleteBtn").click(function () {
        //获取页面中选中的复选框对象
        let flags = $("input[name=flag]:checked");

        if(flags.length == 0){
            alert("请选择需要删除的市场活动数据")
            return;
        }

        //console.log(params);
        //由于删除是一个危险的动作,我们需要给出提示
        if(confirm("确定删除吗?")){
            //没有问题,拼接参数发送请求
            //url?ids=xxx&ids=xxx...
            let params = "";

            for(let i=0; i<flags.length; i++){
                //这样拼接最后会多出一个&,但是不影响功能
                //params += "ids=" + flags[i].value + "&";
                params += "ids=" + flags[i].value;

                if(i < flags.length - 1) params += "&";
            }

            //发送get请求
            get(
                "workbench/activity/deleteActivityList.do?"+params,
                {},
                data=>{
                    if(checked(data))
                        return;

                    //删除成功,刷新列表数据
                    getActivityListPage(1,5);
                }
            )
        }
    })
}
```

* 后台代码 `controller`
```java
    @RequestMapping("/deleteActivityList.do")
    @ResponseBody
    public R deleteActivityList(@RequestParam("ids") List<String> ids){
        checked(ids);

        //修改isDelete属性为1,代表已删除,同时更新修改人和修改时间
        //批量更新操作
        //update tbl_activity set isDelete = 1 where id in (?,?,?)
        //update tbl_activity set isDelete = 1, editBy = ? , editTime = ? where id in (?,?,?)
        //通过遍历,将ids转换为activityList
        List<Activity> activityList = ids.stream()
                .map(id -> new Activity().setId(id).setIsDelete("1").setEditBy(getName()).setEditTime(getTime()))
                .collect(Collectors.toList());

        //批量更新
        return ok(
                activityService.updateIsDelete(activityList),
                State.DB_DELETE_ERROR
        );
    }
}
```

* 后台代码 `service`
```java
@Override
public boolean updateIsDelete(List<Activity> activityList) {
  //for循环删除
  activityList.forEach(
          activity -> {
              int count = activityDao.updateIsDelete(activity);

              if(count<=0)
                  throw new RuntimeException(State.DB_DELETE_ERROR.getMsg());
          }
  );
  return true;
}
```

* Sql `for循环更新`
```xml
<update id="updateIsDelete">
  update tbl_activity set isDelete = #{isDelete}, editTime = #{editTime}, editBy = #{editBy} where id = #{id}
</update>
```

* Sql `一条语句更新,效率高`
```xml
<!--int updateIsDelete(@Param("ids")List<String> ids,@Param("editBy")String editBy,@Param("editTime")String editTime);-->
<update id="updateIsDelete">
    update tbl_activity set isDelete = '1', editTime = #{editTime}, editBy = #{editBy} 
      where id 
    <foreach ...>
       #{id}
    </foreach>
</update>
```

## Excel和POI
### Excel
* 办公软件中的一个组件,通过Excel表格我们可以将数据进行展示

### POI
* Apache提供的一组api,可以通过api来操作办公软件(Excel)
   * 读取Excel文件中的内容
   * 将数据写入到Excel文件中

### Excel和POI对象的关系
* Excel工作簿文件 `xxx.xls 或 xxx.xlsx`
   * 页码
      * 行
         * 单元格
* Workbook `工作簿文件对象`
   * Sheet
      * Row
         * Cell

### POI的测试
```java
//Spring整合Junit单元测试
@RunWith(SpringJUnit4ClassRunner.class)
//运行测试方法时,加载Spring容器
@ContextConfiguration("classpath:spring/applicationContext-service.xml")
public class PoiTest {

    @Test
    public void testReadExcel() throws IOException {
        //读取Excel文件中的内容,输出到控制台即可
        //将Excel文件读取成输入流对象
        InputStream in = new FileInputStream("/Users/limingxuan/Desktop/Activity.xls");

        //基于输入流对象,加载工作簿对象
        /*
            Workbook工作簿对象
                HSSFWorkbook    -> xxx.xls  低版本
                XSSFWorkbook    -> xxx.xlsx 高版本
         */
        Workbook workbook = new HSSFWorkbook(in);

        //获取Excel文件中的行对象
        //获取第一页的数据
        Sheet sheet = workbook.getSheetAt(0);

        //获取最后的行号
        int lastRowNum = sheet.getLastRowNum();

        for (int i = 0; i <= lastRowNum; i++) {
            //获取行对象
            Row row = sheet.getRow(i);

            //获取单元格中的数据,输出到控制台
            //每行只有5个单元格,直接获取
            String name = row.getCell(0).getStringCellValue();
            String startDate = row.getCell(1).getStringCellValue();
            String endDate = row.getCell(2).getStringCellValue();
            String cost = row.getCell(3).getStringCellValue();
            String description = row.getCell(4).getStringCellValue();

            System.out.println(name);
            System.out.println(startDate);
            System.out.println(endDate);
            System.out.println(cost);
            System.out.println(description);
            System.out.println("----------");
        }

        workbook.close();
        in.close();
    }

    @Test
    public void testWriteExcel() throws IOException {
        //将数据写入到Excel文件中
        //创建工作簿对象
        Workbook workbook = new HSSFWorkbook();

        //根据工作簿对象,创建页码对象
        Sheet sheet = workbook.createSheet();

        //根据页码对象,创建行对象
        //第一行是表头数据
        Row r = sheet.createRow(0);

        r.createCell(0).setCellValue("id");
        r.createCell(1).setCellValue("name");
        r.createCell(2).setCellValue("age");

        //for循环,生成数据
        for(int i=0; i<10; i++){
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(i+1);
            row.createCell(1).setCellValue("张"+i);
            row.createCell(2).setCellValue("1"+i);
        }

        //将工作簿对象,输出到指定目录中
        workbook.write(
                //这里一定要指定具体的路径及文件名称
                new FileOutputStream("/Users/limingxuan/Desktop/Activity-Test.xls")
        );
    }
}
```

## 批量导入操作
* 页面
```html
<form id="uploadFile" method="post" enctype="multipart/form-data" action="workbench/activity/uploadActivityFile.do">
    <input type="file" name="activityFile" id="activityFile">
</form>
```

* 前端代码
```javascript
function importActivity() {
    //当点击导入按钮时,我们直接提交表单数据即可
    $("#importActivityBtn").click(function (){
        $("#uploadForm").submit();
    })
}
```

* 后台代码
```java
/*
    批量导入操作 = 文件上传 + 批量新增
 */
@RequestMapping("/uploadActivityFile.do")
public String uploadActivityFile(@RequestParam("activityFile")MultipartFile activityFile) throws IOException {
    //---文件上传---
    //获取文件名称, aa.xls
    String originalFilename = activityFile.getOriginalFilename();

    //获取文件后缀名
    String suffix = originalFilename.substring(
            originalFilename.lastIndexOf(".") + 1
    );

    //校验文件上传的是否是Excel文件
    if(!StringUtils.equalsAny(suffix,"xls","xlsx"))
        throw new RuntimeException(State.UPLOAD_FILE_ERROR.getMsg());

    //校验文件上传的大小,文件小于5MB
    long size = activityFile.getSize() / 1000 / 1000;
    if(size > 5)
        throw new RuntimeException(State.UPLOAD_FILE_SIZE_ERROR.getMsg());

    //判断上传的路径是否存在,如果不存在,则需要创建
    if(!new File(ActivityConstants.UPLOAD_URL).exists())
        new File(ActivityConstants.UPLOAD_URL).mkdirs();

    //文件上传操作
    activityFile.transferTo(
            //指定上传的路径及文件名称
            new File(ActivityConstants.UPLOAD_URL+"Activity-"+getTime()+".xls")
    );
    //---文件上传---

    //---批量导入---
    //---批量导入---
    return "redirect:/workbench/activity/toIndex.do";
}
```