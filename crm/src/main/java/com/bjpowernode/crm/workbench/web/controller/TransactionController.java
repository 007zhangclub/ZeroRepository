package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.workbench.base.Workbench;
import com.bjpowernode.crm.workbench.domain.Tran;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
}
