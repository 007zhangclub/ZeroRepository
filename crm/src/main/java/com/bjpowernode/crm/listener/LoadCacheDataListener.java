package com.bjpowernode.crm.listener;

import com.bjpowernode.crm.settings.domain.DictionaryValue;
import com.bjpowernode.crm.settings.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.Map;

/*
    让当前的类实现ServletContextListener接口
        重写两个方法
            初始化方法 -> 服务器在运行时,我们的listener即被调用,向ServletContext中存入数据
                        当服务器启动完成时,我们的数据已经被加载到ServletContext域对象中
            结束时方法
 */
@Slf4j
public class LoadCacheDataListener implements ServletContextListener {

    //@Autowired
    //private DictionaryService dictionaryService;

    /*
    服务器初始化运行的回调方法
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("加载缓存数据...");

        //将容器中的DictionaryService对象获取到
        //如果通过自动注入的方式,是获取不到的,因为服务器在刚开始运行的时候,我们的容器并没有加载完成
        //所以此时获取到的业务层对象是空
        //我们要通过手动加载容器的方式来后去业务层对象
        ApplicationContext app = new ClassPathXmlApplicationContext("spring/applicationContext-service.xml");

        //从容器中获取service对象
        DictionaryService dictionaryService = app.getBean(DictionaryService.class);

        //获取缓存的数据
        // [
        //  {code:[{DictionaryValue}...]},
        //  {code:[{DictionaryValue}...]},
        //  ...
        // ]
        //List<Map<String,List<DictionaryValue>>> cacheData = dictionaryService.findCacheData();

        //也可以简化存储的数据结构
        /*
            {
                code1:[{DictionaryValue}...],
                code2:[{DictionaryValue}...],
                ...
            }
         */
        Map<String,List<DictionaryValue>> cacheData = dictionaryService.findCacheData();

        //遍历集合数据,将它存入到ServletContext域对象中
        cacheData.forEach(
                (k,v) -> sce.getServletContext().setAttribute(k,v)
        );

        //log.info("DictionaryService : {}",dictionaryService);
        System.out.println("缓存数据加载完成...");
    }
}
