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
<div id="main" style="width: 1200px;height:800px;"></div>

<script>

    $(function () {
        //1. 加载交易阶段数量-漏斗图
        var myChart = echarts.init(document.getElementById('main'));

        get(
            "workbench/chart/transaction/getTranStageData.do",
            {},
            data=>{
                //data:{code:xxx,msg:xxx,success:xxx,data:{nameList:[xxx...],dataList:[{name:xxx,value:xxx}...]}}
                if(checked(data)) return;

                var option = {
                    title: {
                        text: 'Funnel'
                    },
                    tooltip: {
                        trigger: 'item',
                        formatter: '{a} <br/>{b} : {c}%'
                    },
                    toolbox: {
                        feature: {
                            dataView: { readOnly: false },
                            restore: {},
                            saveAsImage: {}
                        }
                    },
                    legend: {
                        //data: ['Show', 'Click', 'Visit', 'Inquiry', 'Order']
                        data: data.data.nameList
                    },
                    series: [
                        {
                            name: 'Funnel',
                            type: 'funnel',
                            left: '10%',
                            top: 60,
                            bottom: 60,
                            width: '80%',
                            min: 0,
                            max: 100,
                            minSize: '0%',
                            maxSize: '100%',
                            sort: 'descending',
                            gap: 2,
                            label: {
                                show: true,
                                position: 'inside'
                            },
                            labelLine: {
                                length: 10,
                                lineStyle: {
                                    width: 1,
                                    type: 'solid'
                                }
                            },
                            itemStyle: {
                                borderColor: '#fff',
                                borderWidth: 1
                            },
                            emphasis: {
                                label: {
                                    fontSize: 20
                                }
                            },
                            // data: [
                            //     { value: 60, name: 'Visit' },
                            //     { value: 40, name: 'Inquiry' },
                            //     { value: 20, name: 'Order' },
                            //     { value: 80, name: 'Click' },
                            //     { value: 100, name: 'Show' }
                            // ]
                            data: data.data.dataList
                        }
                    ]
                };

                myChart.setOption(option);
            }
        )
    })
</script>
</body>
</html>
