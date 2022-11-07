package com.bjpowernode.crm.utils;

import java.util.UUID;

public class IdUtils {
    public static String getId(){
        //通过UUID的方式生成32位的随机字符串
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
