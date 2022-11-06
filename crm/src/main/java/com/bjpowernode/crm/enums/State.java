package com.bjpowernode.crm.enums;

import lombok.*;

/*
当前枚举封装公共的返回值结果集的code码和msg信息
 */
//@Setter 添加该注解后,当前实体类就有set方法了
@Getter //添加该注解后,当前实体类就有get方法了
@NoArgsConstructor
@AllArgsConstructor
public enum State {

    /*
    公共的状态码和返回值结果集
     */
    SUCCESS(20000,"请求成功"),
    FAILED(20001,"请求失败"),
    STATE_CODE_ERROR(20002,"状态码信息异常"),
    PARAMS_ERROR(20003,"参数信息传递异常"),


    /*
    用户模块的状态码和返回值结果集
     */
    USER_NOT_EXISTS(20101,"用户查询异常"),
    USER_LOGIN_ACT_OR_LOGIN_PWD_ERROR(20102,"用户名或密码错误"),
    USER_ACCOUNT_EXPIRED_ERROR(20103,"当前账号已过期,请联系管理人员"),
    USER_ACCOUNT_LOCKED_ERROR(20104,"当前账号已锁定,请联系管理人员"),
    USER_ACCOUNT_IP_NOT_ALLOW_ERROR(20105,"当前账号IP地址受限,请联系管理人员"),
    USER_NO_AUTHORIZATION(20106,"当前用户未登录"),


    ;

    private Integer code;
    private String  msg;

    /*
    根据msg信息获取code码
     */
    public static Integer getCodeByMsg(String msg){
        //根据所有的枚举值,遍历获取code码
        for (State state : State.values()) {
            if(state.getMsg().equals(msg))
                return state.getCode();
        }

        throw new RuntimeException(STATE_CODE_ERROR.getMsg());
    }
}
