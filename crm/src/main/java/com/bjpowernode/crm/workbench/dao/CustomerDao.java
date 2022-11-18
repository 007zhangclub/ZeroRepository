package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Customer;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CustomerDao {

    @Select("select * from tbl_customer where name = #{customerName}")
    Customer findByName(String customerName);

    int insert(Customer customer);

    @Select("select name from tbl_customer where name like '%' #{customerName} '%'")
    List<String> getCustomerNameList(String customerName);
}
