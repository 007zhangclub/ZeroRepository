function selectAll() {
    $("#selectAllBtn").click(function () {
        //根据全选框的选中状态,加载所有的复选框的选中状态
        $("input[name=flag]").prop("checked",this.checked)
    })
}


function reverseAll() {
    $("input[name=flag]").click(function () {
        //所有的复选框都选中之后,默认选中全选框
        $("#selectAllBtn").prop("checked",$("input[name=flag]").length == $("input[name=flag]:checked").length)
    })
}