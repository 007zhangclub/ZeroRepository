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


function load(jqueryObj,data,callBack){
    //1. 定义字符串,用于封装标签数据
    let html = "";

    //2. 遍历集合数据,将标签的模板加载为动态的数据
    $.each(data.data,function (i,n) {
        html += callBack(i,n);
    })

    //3. 将html字符串,加载到标签容器中
    $(jqueryObj).html(html);
}


function loadPage(jqueryObj,data,callBack){
    //1. 定义字符串,用于封装标签数据
    let html = "";

    //2. 遍历集合数据,将标签的模板加载为动态的数据
    $.each(data.records,function (i,n) {
        html += callBack(i,n);
    })

    //3. 将html字符串,加载到标签容器中
    $(jqueryObj).html(html);
}


function loadHtml(jqueryObj,data,callBack,initHtml){
    //1. 定义字符串,用于封装标签数据
    let html = "";

    //2. 遍历集合数据,将标签的模板加载为动态的数据
    $.each(data.data,function (i,n) {
        //初始化标签
        if (i==0) html += initHtml;

        html += callBack(i,n);
    })

    //3. 将html字符串,加载到标签容器中
    $(jqueryObj).html(html);
}