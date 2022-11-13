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