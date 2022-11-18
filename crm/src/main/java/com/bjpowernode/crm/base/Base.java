package com.bjpowernode.crm.base;

import com.bjpowernode.crm.entity.R;
import com.bjpowernode.crm.enums.State;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.utils.IdUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class Base {

    protected boolean checked(String... params){
        //遍历参数列表,判断非空
        for (String param : params)
            if(StringUtils.isBlank(param))
                throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        return true;
    }

    protected <T> boolean checked(T data){
        //校验对象的合法性
        if(ObjectUtils.isEmpty(data))
            throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        return true;
    }

    protected boolean checked(Collection<?> data){
        //校验集合的合法性
        if(CollectionUtils.isEmpty(data))
            throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        return true;
    }

    protected boolean checked(Map<?,?> data){
        //校验集合的合法性
        if(CollectionUtils.isEmpty(data))
            throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        return true;
    }


    protected <T> boolean checkData(T data){
        //判断data的数据类型
        if(data instanceof String)
            //String类型
            if(StringUtils.isBlank((String)data))
                throw new RuntimeException(State.PARAMS_ERROR.getMsg());
        if(data instanceof Collection)
            //校验对象的合法性
            if(CollectionUtils.isEmpty((Collection<?>) data))
                throw new RuntimeException(State.PARAMS_ERROR.getMsg());
        if(data instanceof Map)
            //校验对象的合法性
            if(CollectionUtils.isEmpty((Map<?, ?>) data))
                throw new RuntimeException(State.PARAMS_ERROR.getMsg());
        if(data instanceof Object)
            if(ObjectUtils.isEmpty(data))
                throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        return true;
    }


    public R<?> ok(){
        return R.builder()
                .code(State.SUCCESS.getCode())
                .msg(State.SUCCESS.getMsg())
                .success(true)
                .build();
    }


    public R<?> ok(State state){
        return R.builder()
                .code(state.getCode())
                .msg(state.getMsg())
                .success(true)
                .build();
    }

    public <T> R ok(T data){
        return R.builder()
                .code(State.SUCCESS.getCode())
                .msg(State.SUCCESS.getMsg())
                .success(true)
                .data(data)
                .build();
    }

    public <T> R<?> ok(boolean flag,State state){
        if(flag)
            return R.builder()
                    .code(State.SUCCESS.getCode())
                    .msg(State.SUCCESS.getMsg())
                    .success(true)
                    .build();

        throw new RuntimeException(state.getMsg());

    }


    public <T> R<?> ok(State state, T data){
        return R.builder()
                .code(state.getCode())
                .msg(state.getMsg())
                .success(true)
                .data(data)
                .build();
    }


    public <T> R<?> ok(State state, T data,boolean success){
        return R.builder()
                .code(state.getCode())
                .msg(state.getMsg())
                .success(success)
                .data(data)
                .build();
    }


    public R<?> err(){
        return R.builder()
                .code(State.FAILED.getCode())
                .msg(State.FAILED.getMsg())
                .success(false)
                .build();
    }


    public R<?> err(State state){
        return R.builder()
                .code(state.getCode())
                .msg(state.getMsg())
                .success(false)
                .build();
    }

    public <T> R<?> okAndCheck(T data){

        if(ObjectUtils.isEmpty(data))
            throw new RuntimeException(State.PARAMS_ERROR.getMsg());

        return R.builder()
                .code(State.SUCCESS.getCode())
                .msg(State.SUCCESS.getMsg())
                .success(true)
                .data(data)
                .build();
    }


    protected String getId(){
        return IdUtils.getId();
    }

    @Autowired
    private HttpSession session;

    protected String getOwner(){
        return ((User) session.getAttribute("user")).getId();
    }

    protected String getName(){
        return ((User) session.getAttribute("user")).getName();
    }

    protected String getTime(){
        //return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
