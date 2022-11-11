# Crm项目笔记
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

    String url = ActivityConstants.UPLOAD_URL+"Activity-"+getTime()+".xls";
    //文件上传操作
    activityFile.transferTo(
    //指定上传的路径及文件名称
    new File(url)
    );
    //---文件上传---

    List<Activity> activityList = new ArrayList<>();

    //---批量导入---
    //将Excel文件加载成输入流对象
    InputStream in = new FileInputStream(url);

    //基于输入流对象,创建工作簿对象
    Workbook workbook = new HSSFWorkbook(in);

    //通过工作簿对象,获取页码对象
    Sheet sheet = workbook.getSheetAt(0);

    //通过页码对象,获取最后的行号
    int lastRowNum = sheet.getLastRowNum();

    //遍历每一行的数据,必须要跳过第一行,因为第一行是表头数据
    for(int i=0; i<lastRowNum; i++){
    //根据页码对象,获取每一行的对象
    Row row = sheet.getRow(i + 1);

    //获取行中单元格的数据
    String activityName = row.getCell(0).getStringCellValue();
    String startDate = row.getCell(1).getStringCellValue();
    String endDate = row.getCell(2).getStringCellValue();
    String cost = row.getCell(3).getStringCellValue();
    String description = row.getCell(4).getStringCellValue();

    //封装到集合中
    activityList.add(
    //封装市场活动对象
    Activity.builder()
    .id(IdUtils.getId())
    .owner(getOwner())
    .name(activityName)
    .startDate(startDate)
    .endDate(endDate)
    .cost(cost)
    .description(description)
    .createTime(getTime())
    .createBy(getName())
    .editTime(getTime())
    .editBy(getName())
    .isDelete("0")
    .build()
    );
    }

    //完成批量导入的操作
    boolean flag = activityService.saveActivityList(activityList);

    if(!flag)
    throw new RuntimeException(State.DB_SAVE_ERROR.getMsg());

    //---批量导入---
    return "redirect:/workbench/activity/toIndex.do";
}
```

* Sql
```xml
<sql id="listValues">
    #{a.id},
    #{a.owner},
    #{a.name},
    #{a.startDate},
    #{a.endDate},
    #{a.cost},
    #{a.description},
    #{a.createTime},
    #{a.createBy},
    #{a.editTime},
    #{a.editBy},
    #{a.isDelete}
</sql>

<insert id="insertList">
    insert into tbl_activity
    (
    <include refid="star"/>
    )
    values
           <foreach collection="list" separator="," item="a">
               (
               <include refid="listValues"/>
               )
           </foreach>
</insert>
```


## 批量导出
* 前端代码
```javascript
function exportActivityAll() {
    $("#exportActivityAllBtn").click(function () {
        //需要给出一个提示操作,以免用户误操作,直接下了文件
        if(confirm("确定要导出全部数据吗?"))
            //这里不需要异步发送请求了,因为我们要通过response对象来响应下载的文件
            to("workbench/activity/exportActivity.do");
    })
}
```

* 后台代码
```java
/*
    批量导出操作
        由于我们的文件需要通过response对象进行相应回浏览器的操作
        我们这里不需要返回值void
 */
@RequestMapping("/exportActivity.do")
public void exportActivity(HttpServletResponse response) throws IOException {
    //查询数据库的所有未删除的市场活动列表数据
    List<Activity> activityList = activityService.findActivityList();

    if(CollectionUtils.isEmpty(activityList))
        //如果查询出的数据为空,那么也无需下载了
        return;

    //遍历数据,封装到Workbook对象中
    Workbook workbook  = new HSSFWorkbook();

    //基于工作簿对象,创建页码对象
    Sheet sheet = workbook.createSheet("市场活动列表");

    //基于页码对象,创建行对象,第一行是表头数据
    Row r0 = sheet.createRow(0);

    /*
    id
    owner
    name
    startDate
    endDate
    cost
    description
    createTime
    createBy
    editTime
    editBy
    isDelete
    username
     */
    r0.createCell(0).setCellValue("唯一标识");
    r0.createCell(1).setCellValue("用户标识");
    r0.createCell(2).setCellValue("用户名称");
    r0.createCell(3).setCellValue("活动名称");
    r0.createCell(4).setCellValue("开始时间");
    r0.createCell(5).setCellValue("结束时间");
    r0.createCell(6).setCellValue("成本");
    r0.createCell(7).setCellValue("描述");
    r0.createCell(8).setCellValue("创建时间");
    r0.createCell(9).setCellValue("创建人");
    r0.createCell(10).setCellValue("修改时间");
    r0.createCell(11).setCellValue("修改人");
    r0.createCell(12).setCellValue("状态");

    //遍历集合数据,封装数据到行和单元格对象中
    for (int i = 0; i < activityList.size(); i++) {
        Activity activity = activityList.get(i);

        //创建每一行的对象,封装单元格数据
        Row row = sheet.createRow(i + 1);

        row.createCell(0).setCellValue(activity.getId());
        row.createCell(1).setCellValue(activity.getOwner());
        row.createCell(2).setCellValue(activity.getUsername());
        row.createCell(3).setCellValue(activity.getName());
        row.createCell(4).setCellValue(activity.getStartDate());
        row.createCell(5).setCellValue(activity.getEndDate());
        row.createCell(6).setCellValue(activity.getCost());
        row.createCell(7).setCellValue(activity.getDescription());
        row.createCell(8).setCellValue(activity.getCreateTime());
        row.createCell(9).setCellValue(activity.getCreateBy());
        row.createCell(10).setCellValue(activity.getEditTime());
        row.createCell(11).setCellValue(activity.getEditBy());
        row.createCell(12).setCellValue(activity.getIsDelete().equals("0")?"正常":"");
    }

    //设置下载的文件名称
    response.addHeader(
            FileUploadBase.CONTENT_DISPOSITION,
            FileUploadBase.ATTACHMENT+";filename=Activity-"+getTime()+".xls"
    );

    //下载操作
    workbook.write(
            //通过response对象,获取输出流对象
            response.getOutputStream()
    );
}
```

* Sql
```xml
<select id="findAll" resultType="com.bjpowernode.crm.workbench.domain.Activity">
    select a.*,u.name as username from tbl_activity a left join tbl_user u on a.owner = u.id where a.isDelete = '0'
</select>
```