package com.bjpowernode.crm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/*
lombok注解来生成实体类中的属性
    Data : 自动生成get/set/toString等方法
    NoArgsConstructor : 自动生成无参构造
    AllArgsConstructor : 自动生成全参构造
    Builder : 通过构建者模式来创建实体类对象
        RBuilder(返回值)来封装条件
        使用
            R.builder()
             .code().msg().success().data()
             .build();
    Accessors : 通过链式的方式来封装实体类对象
        new R().setCode().setMsg().setSuccess().setData();
    JsonInclude : NON_NULL,将实体类中的null值进行过滤,不返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {
    private Integer code;
    private String  msg;
    private boolean success;
    private T data;
    //private Object data;
}
