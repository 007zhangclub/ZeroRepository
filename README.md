# Crm项目笔记
## 加载转换页面基本数据
* 前端代码
```javascript
<button type="button" class="btn btn-default" onclick="window.location.href='workbench/clue/toConvert.do?clueId=${clue.id}';"><span class="glyphicon glyphicon-retweet"></span> 转换</button>
```

* 后台代码
```java
@RequestMapping("/toConvert.do")
public String toConvert(@RequestParam("clueId")String clueId,Model model){
    //根据线索id查询线索数据
    Clue clue = clueService.findClue(clueId);

    if(ObjectUtils.isNotEmpty(clue))
        model.addAttribute("clue",clue);

    return "/workbench/clue/convert";
}
```

## 加载已关联的市场活动列表数据
* 前端代码
```javascript
function openSearchActivityModal() {
    $("#openSearchActivityModalBtn").click(function () {
        getRelationList();
        //打开模态窗口
        $("#searchActivityModal").modal("show");
    })
}

function getRelationList() {
    //发送请求获取数据
    get(
        "workbench/clue/getClueActivityRelationList.do",
        {
            clueId:$("#clueId").val(),
            activityName:$("#searchActivityInput").val()
        },data=>{
            if(checked(data))
                return;

            //异步加载
            load(
                $("#activityListBody"),
                data,
                (i,n)=>{
                    return  '<tr>'+
                        '<td><input type="radio" name="activity" value="'+n.id+'"/></td>'+
                        '<td id="n_'+n.id+'">'+n.name+'</td>'+
                        '<td>'+n.startDate+'</td>'+
                        '<td>'+n.endDate+'</td>'+
                        '<td>'+n.username+'</td>'+
                        '</tr>';
                }
            )
        }
    )
}
```

* 后台代码
```java
@RequestMapping("/getClueActivityRelationList.do")
@ResponseBody
public R getClueActivityRelationList(@RequestParam("clueId") String clueId,
                                     @RequestParam(value = "activityName",required = false)String activityName) {
    //根据线索id查询当前线索已关联的市场活动列表数据
    List<Activity> activityList = clueService.findClueActivityRelationList(clueId,activityName);

    return ok(activityList);
}
```

* Sql
```xml
<select id="findActivityRelationListLike" resultType="com.bjpowernode.crm.workbench.domain.Activity">
    select car.id as carId, u.name as username, a.*
    from tbl_activity a
    left join tbl_user u on a.owner = u.id
    left join tbl_clue_activity_relation car on a.id = car.activityId
    where a.isDelete = '0'
    and car.clueId = #{clueId}
    <if test="activityName != null and activityName != ''">
        and a.name like '%' #{activityName} '%'
    </if>
</select>
```

## 模糊查询已关联的市场活动列表数据
* 前端代码
```javascript
function searchActivityList() {
    $("#searchActivityInput").keydown(function (event) {
        if(event.keyCode == 13){
            getRelationList();

            return false;
        }
    })
}
```

## 回显市场活动数据
```javascript
function showActivity() {
    $("#addRelationBtn").click(function () {
        //获取当前已选中的市场活动数据
        let activities = $("input[name=activity]:checked");

        if(activities.length != 1){
            //没有选中或选中多条记录
            alert("请选择一条需要关联的记录");
            return;
        }

        //获取市场活动id
        let activityId = activities[0].value;

        //获取市场活动名称
        let activityName = $("#n_"+activityId).html();

        //回显数据,将id存入到隐藏域中
        $("#activityId").val(activityId);

        $("#activity").val(activityName);

        //关闭模态窗口
        $("#searchActivityModal").modal("hide");
    })
}
```