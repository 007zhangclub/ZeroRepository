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