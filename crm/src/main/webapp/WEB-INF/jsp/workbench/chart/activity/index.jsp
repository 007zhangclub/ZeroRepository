<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>入门案例</title>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/ECharts/echarts.min.js"></script>
    <script type="text/javascript" src="js/ajax.js"></script>

</head>
<body>

<%--引入标签容器--%>
<div id="main" style="width: 600px;height:400px;"></div>

<script>
    var myChart = echarts.init(document.getElementById('main'));

    var option = {
        title: {
            //标题内容
            text: 'ECharts 入门示例'
        },
        tooltip: {},
        legend: {
            //分类
            data:['销量']
        },
        xAxis: {
            //X轴的数据,每个不同的分类
            data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
        },
        yAxis: {},
        series: [{
            //对应展示的分类选项
            name: '销量',
            //图标类型,柱状图
            type: 'bar',
            //每个x轴所对应展示的数据值
            data: [5, 20, 36, 10, 10, 20]
        }]
    };

    myChart.setOption(option);
</script>
</body>
</html>
