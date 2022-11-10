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