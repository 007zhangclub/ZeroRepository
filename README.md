# Crm项目笔记
## 加载线索详情页面的市场活动列表数据 `关联数据`
> 加载当前线索已关联的市场活动列表数据
> 线索和市场活动是多对多的关系
* 前端代码
```javascript
function getClueActivityRelationList() {
    get(
        "workbench/clue/getClueActivityRelationList.do",
        {
            clueId:$("#hidden-clueId").val()
        },data=>{
            if(checked(data))
                return;
            //异步加载
            load(
                $("#relationListBody"),
                data,
                (i,n) => {
                    return  '<tr>'+
                            '<td>'+n.name+'</td>'+
                            '<td>'+n.startDate+'</td>'+
                            '<td>'+n.endDate+'</td>'+
                            '<td>'+n.username+'</td>'+
                            '<td><a href="javascript:void(0);" style="text-decoration: none;"><span class="glyphicon glyphicon-remove"></span>解除关联</a></td>'+
                            '</tr>';
                }
            )
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

* Sql
```java
@Select({
    "select car.id as carId,u.name as username,a.* from tbl_activity a " ,
    "left join tbl_user u on a.owner = u.id " ,
    "left join tbl_clue_activity_relation car on a.id = car.activityId " ,
    "where a.isDelete = '0' and car.clueId = #{clueId}"
})
List<Activity> findActivityRelationList(String clueId);
```

## 加载线索详情页面的未关联的市场活动列表数据 `未关联数据`
* 前端代码
```javascript
function openBundModal() {
    $("#openBundModalBtn").click(function () {
        //根据线索id,查询当前线索没有关联的市场活动列表数据
        get(
            "workbench/clue/getClueActivityUnRelationList.do",
            //还有模糊查询的条件
            {
                clueId:$("#hidden-clueId").val(),
                activityName:$("#searchActivity").val()
            },
            data=>{
                if(checked(data))
                    return;
                //异步加载,打开模态窗口
                load(
                    $("#unRelationListBody"),
                    data,
                    (i,n) => {
                        return  '<tr>'+
                                '<td><input type="checkbox"/></td>'+
                                '<td>'+n.name+'</td>'+
                                '<td>'+n.startDate+'</td>'+
                                '<td>'+n.endDate+'</td>'+
                                '<td>'+n.username+'</td>'+
                                '</tr>';
                    }
                )
                $("#bundModal").modal("show");
            }
        )
    })
}
```

* 后台代码
```java
@RequestMapping("/getClueActivityUnRelationList.do")
@ResponseBody
public R getClueActivityUnRelationList(@RequestParam("clueId")String clueId,
                                       @RequestParam(value = "activityName",required = false)String activityName){

    //根据线索id查询未关联的市场活动列表数据
    List<Activity> activityList = clueService.findClueActivityUnRelationList(clueId);

    if(CollectionUtils.isEmpty(activityList)){
        //如果查询出的结果为空,证明所有的市场活动列表数据都可以进行关联,我们返回所有的市场活动列表数据
        activityList = activityService.findAll();

    }else{
        //如果查询出的结果不为空,并且市场活动名称不为空我们去模糊查询
        if(StringUtils.isNotBlank(activityName))
            //模糊查询
            activityList = clueService.findClueActivityUnRelationList(clueId,activityName);
    }

    return ok(activityList);
}
```

* Sql
```xml
<select id="findClueActivityUnRelationListLike" resultType="com.bjpowernode.crm.workbench.domain.Activity">
    select car.id as carId, u.name as username, a.*
    from tbl_activity a
    left join tbl_user u on a.owner = u.id
    left join tbl_clue_activity_relation car on a.id != car.activityId
    <where>a.isDelete = '0' and car.clueId = #{clueId}
        <if test="activityName != null and activityName != ''">
            and a.name like '%' #{activityName} '%'
        </if>
    </where>
</select>
```