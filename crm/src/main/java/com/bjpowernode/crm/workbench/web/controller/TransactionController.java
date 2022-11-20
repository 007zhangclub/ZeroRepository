package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/workbench/transaction")
public class TransactionController extends Workbench {
    /*
        跳转到交易首页面
     */
    @RequestMapping("/toIndex.do")
    public String toIndex(){
        return "/workbench/transaction/index";
    }


    @RequestMapping("/toSave.do")
    public String toSave(Model model){

        //查询所有的用户列表数据
        List<User> userList = userService.findUserList();

        if(!CollectionUtils.isEmpty(userList))
            model.addAttribute("userList",userList);

        return "/workbench/transaction/save";
    }


    @RequestMapping("/getCustomerName.do")
    @ResponseBody
    public R<List<String>> getCustomerName(@RequestParam("customerName")String customerName){
        //根据客户名称,模糊查询获取客户名称的列表数据,仅限于名称,其他不需要
        List<String> customerNameList = customerService.findCustomerNameList(customerName);

        return ok(customerNameList);
    }


    @RequestMapping("/saveTransaction.do")
    @ResponseBody
    public R<Boolean> saveTransaction(@RequestBody Tran tran,
                                      @RequestParam(value = "customerName")String customerName){
        //校验
        checked(
                tran.getOwner(),
                tran.getStage(),
                tran.getName(),
                tran.getExpectedDate(),
                customerName
        );

        //新增交易操作
        boolean flag = transactionService.saveTransaction(tran,customerName,getName(),getTime());

        return ok(flag);
    }


    @RequestMapping("/toDetail.do")
    public String toDetail(@RequestParam("id") String id, Model model, HttpServletRequest request){
        //根据交易id查询交易数据
        Tran tran = transactionService.findTransaction(id);

        if(ObjectUtils.isNotEmpty(tran)){

            model.addAttribute("tran",tran);

            //加载可能性数据
            //根据request对象获取服务器缓存数据
            Map<String,String> sapMap = (Map<String, String>) request.getServletContext().getAttribute("sapMap");

            String stage = tran.getStage();

            if(StringUtils.isNotBlank(stage))
                model.addAttribute("possibility",sapMap.get(stage));
        }

        return "/workbench/transaction/detail";
    }


    @RequestMapping("/getTranHistoryList.do")
    @ResponseBody
    public R<List<TranHistory>> getTranHistoryList(@RequestParam("tranId")String tranId,HttpServletRequest request){

        //获取阶段和可能性的map集合
        Map<String,String> sapMap = (Map<String, String>) request.getServletContext().getAttribute("sapMap");

        //查询交易历史记录
        List<TranHistory> tranHistoryList = transactionService.findTransactionHistoryList(tranId);

        if(!CollectionUtils.isEmpty(tranHistoryList)){
            //处理可能性数据
            tranHistoryList.forEach(history -> history.setPossibility(sapMap.get(history.getStage())));
        }

        return ok(tranHistoryList);
    }


    @RequestMapping("/updateStage.do")
    @ResponseBody
    public R updateStage(@RequestParam("tranId")String tranId,
                              @RequestParam("stage")String stage,
                              @RequestParam("money")String money,
                              @RequestParam("expectedDate")String expectedDate){
        checked(
                tranId,stage,money,expectedDate
        );

        //更新和新增操作
        transactionService.updateStage(tranId,money,expectedDate,getName(),getTime(),stage);

        return ok();
    }
}
