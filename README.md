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