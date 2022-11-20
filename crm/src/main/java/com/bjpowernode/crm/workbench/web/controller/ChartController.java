package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.workbench.base.Workbench;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workbench/chart")
public class ChartController extends Workbench {

    @RequestMapping("/activity/toIndex.do")
    public String toActivityIndex(){
        return "/workbench/chart/activity/index";
    }

    @RequestMapping("/clue/toIndex.do")
    public String toClueIndex(){
        return "/workbench/chart/clue/index";
    }

    @RequestMapping("/customerAndContacts/toIndex.do")
    public String toCustomerAndContactsIndex(){
        return "/workbench/chart/customerAndContacts/index";
    }

    @RequestMapping("/transaction/toIndex.do")
    public String toTransactionIndex(){
        return "/workbench/chart/transaction/index";
    }


    @RequestMapping("/transaction/getTranStageData.do")
    @ResponseBody
    public R getTranStageData(){
        //查询每个交易阶段的数量,这里我们查询的是交易历史表,因为交易表没有这么多的记录
        //每个map集合是{name:xxx,value:xxx}
        List<Map<String,Object>> dataList = transactionService.getChartDate();

        //将集合中的数据提取出来,组装成nameList
        List<Object> nameList = dataList.stream()
                //提取数据,map参数,代表遍历的每个map集合
                //将map集合中的name属性值,提取出来
                //将提取出来的数据,收集成一个List集合并返回
                .map(map -> map.get("name")).collect(Collectors.toList());

        //组装返回值结果集
        Map<String,List> resultMap = new HashMap<>();
        resultMap.put("nameList",nameList);
        resultMap.put("dataList",dataList);

        return ok(resultMap);
    }
}
