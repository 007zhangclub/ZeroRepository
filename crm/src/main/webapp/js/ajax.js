/*
get方式以地址栏拼接键值对的方式传递数据
 */
function get(url,data,successCallBack,errorCallBack) {
    $.ajax({
        url:url,
        data:data,
        type:"get",
        contentType: "application/x-www-form-urlencoded", //默认是以表单形式传递
        dataType: "json",//服务器预计返回的参数类型
        success:successCallBack,
        error:errorCallBack
    })
}


/*
post方式传递json数据
 */
function post(url,data,successCallBack,errorCallBack) {
    $.ajax({
        url:url,
        data:JSON.stringify(data),
        type:"post",
        contentType: "application/json;charset=UTF-8", //前端以json的方式来传递数据
        dataType: "json",//服务器预计返回的参数类型
        success:successCallBack,
        error:errorCallBack
    })
}


/*
post方式提交表单参数
 */
function post4m(url,data,successCallBack,errorCallBack) {
    $.ajax({
        url:url,
        data:data,
        type:"post",
        contentType: "application/x-www-form-urlencoded", //默认是以表单形式传递
        dataType: "json",//服务器预计返回的参数类型
        success:successCallBack,
        error:errorCallBack
    })
}


function to(url) {
    window.location.href = url;
}


function checked(data){
    if(!data.success){
        alert(data.msg);
    }
    return !data.success;
}