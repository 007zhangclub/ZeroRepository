function openCreateClueModal() {
    //点击创建按钮,打开模态窗口,加载数据
    $("#openCreateClueModalBtn").click(function () {
        //获取用户的列表数据,加载到所有者的下拉框中
        get(
            "settings/user/getUserList.do",
            {},
            data=>{
                if(checked(data))
                    return;

                //异步加载
                loadHtml(
                    $("#create-owner"),
                    data,
                    (i,n)=>{
                        return "<option value="+n.id+">"+n.name+"</option>"
                    },"<option></option>"
                )

                //默认选中当前登录的用户
                $("#create-owner").val($("#userId").val());

                //打开模态窗口
                $("#createClueModal").modal("show");
            }
        )
    })
}


function initDateTimePicker() {
    //加载一个日历控件的样式
    $(".time").datetimepicker({
        //最小的视图是月份
        minView: "month",
        //中文显示
        language:  'zh-CN',
        //提交参数的日期格式
        format: 'yyyy-mm-dd',
        //是否支持自动关闭
        autoclose: true,
        //是否支持今天按钮,点击后可以加载到今天的日期
        todayBtn: true,
        //组件显示位置,左下方
        pickerPosition: "top-left"
    });

    $(".time1").datetimepicker({
        //最小的视图是月份
        minView: "month",
        //中文显示
        language:  'zh-CN',
        //提交参数的日期格式
        format: 'yyyy-mm-dd',
        //是否支持自动关闭
        autoclose: true,
        //是否支持今天按钮,点击后可以加载到今天的日期
        todayBtn: true,
        //组件显示位置,左下方
        pickerPosition: "bottom-left"
    });
}



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


function getClueRemarkList() {
    //发送请求获取列表数据,根据clueId来获取
    let clueId = $("#hidden-clueId").val();
    
    if(clueId == ""){
        alert("页面加载异常,请刷新后再试")
        return;
    }
    
    get(
        "workbench/clue/getClueRemarkList.do",
        {
            clueId:clueId
        },data=>{
            if(checked(data))
                return;
            //异步加载
            load(
                $("#clueRemarkListBody"),
                data,
                (i,n) => {
                    return  '<div class="remarkDiv" style="height: 60px;">'+
                            '<img title="zhangsan" src="image/user-thumbnail.png" style="width: 30px; height:30px;">'+
                            '<div style="position: relative; top: -40px; left: 40px;">'+
                            '<h5>'+n.noteContent+'</h5>'+
                            '<font color="gray">线索</font> <font color="gray">-</font> <b>'+($("#hidden-fullname").val()+$("#hidden-appellation").val()+$("#hidden-compney").val())+'李四先生-动力节点</b> <small style="color: gray;"> '+(n.editFlag==0?n.createTime:n.editTime)+' 由 '+(n.editFlag==0?n.createBy:n.editBy)+'</small>'+
                            '<div style="position: relative; left: 500px; top: -30px; height: 30px; width: 100px; display: none;">'+
                            '<a class="myHref" href="javascript:void(0);"><span class="glyphicon glyphicon-edit" style="font-size: 20px; color: #E6E6E6;"></span></a>'+
                            '&nbsp;&nbsp;&nbsp;&nbsp;'+
                            '<a class="myHref" href="javascript:void(0);"><span class="glyphicon glyphicon-remove" style="font-size: 20px; color: #E6E6E6;"></span></a>'+
                            '</div>'+
                            '</div>'+
                            '</div>';
                }
            )
        }
    )
}


function saveClueRemark() {
    $("#saveClueRemarkBtn").click(function () {
        //获取备注信息
        let noteContent = $("#remark").val();

        if(noteContent == ""){
            alert("备注信息不能为空");
            return;
        }

        //新增操作,携带clueId参数
        let clueId = $("#hidden-clueId").val();

        //发送post请求
        post(
            "workbench/clue/remark/saveClueRemark.do",
            {
                noteContent:noteContent,
                clueId:clueId
            },data=>{
                if(checked(data))
                    return;
                //异步加载,刷新列表数据
                getClueRemarkList();

                $("#remark").val("");
            }
        )
    })
}


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
                            '<td><a onclick="deleteClueActivityRelation(\''+n.carId+'\')" href="javascript:void(0);" style="text-decoration: none;"><span class="glyphicon glyphicon-remove"></span>解除关联</a></td>'+
                            '</tr>';
                }
            )
        }
    )
}



function deleteClueActivityRelation(carId) {
    //删除必须提示
    if(confirm("确定要删除吗?"))
        get(
            "workbench/clue/deleteClueActivityRelation.do",
            {carId:carId},
            data=>{
                if(checked(data))
                    return;
                //删除成功,刷新列表数据
                getClueActivityRelationList()
            }
        )
}



function openBundModal() {
    $("#openBundModalBtn").click(function () {
        //加载未关联的市场活动列表数据
        clueActivityUnRelationList();
        //打开模态窗口
        $("#bundModal").modal("show");
    })
}

function clueActivityUnRelationList() {
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
            //异步加载
            load(
                $("#unRelationListBody"),
                data,
                (i,n) => {
                    return  '<tr>'+
                        '<td><input type="checkbox" name="flag" value="'+n.id+'"/></td>'+
                        '<td>'+n.name+'</td>'+
                        '<td>'+n.startDate+'</td>'+
                        '<td>'+n.endDate+'</td>'+
                        '<td>'+n.username+'</td>'+
                        '</tr>';
                }
            )

        }
    )
}

function searchClueActivityUnRelationList() {
    //给模糊查询的输入框绑定键盘按下的事件
    $("#searchActivity").keydown(event=>{
        //按下了回车键
        if(event.keyCode == 13){
            //发送请求模糊查询
            clueActivityUnRelationList();
            //返回false,阻止页面的整体的表单提交
            return false;
        }
    })
}


function saveClueActivityRelationList() {
    $("#saveClueActivityRelationListBtn").click(function () {
        //获取选中的市场活动的ids
        let flags = $("input[name=flag]:checked");

        if(flags.length == 0){
            alert("请选择需要关联的数据");
            return;
        }

        //获取参数ids
        let params = [];

        for(let i=0; i<flags.length; i++){
            params.push(flags[i].value);
        }

        //获取线索id
        let clueId = $("#hidden-clueId").val();

        //发送请求
        post(
            "workbench/clue/saveClueActivityRelationList.do?clueId="+clueId,
            params,
            data=>{
                if(checked(data))
                    return;

                //刷新已关联的市场活动列表数据
                getClueActivityRelationList();

                //关闭模态窗口
                $("#bundModal").modal("hide");
            }
        )
    })
}


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


function searchActivityList() {
    $("#searchActivityInput").keydown(function (event) {
        if(event.keyCode == 13){
            getRelationList();

            return false;
        }
    })
}


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


function clueConvert() {
    $("#clueConvertBtn").click(function () {
        //根据复选框的选中状态,来决定是创建交易的线索转换还是没有创建交易的线索转换操作
        if($("#isCreateTransaction").prop("checked"))
            //选中,创建交易的线索转换操作
            $("#convertForm").submit();
        else
            //未选中,没有创建交易的线索转换操作
            to("workbench/clue/clueConvert.do?clueId="+$("#clueId").val())
    })
}