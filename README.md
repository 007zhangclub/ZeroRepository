# Crm项目笔记

## 环境搭建
### pom.xml
```xml
<dependencies>
    <!--Servlet-->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.1</version>
      <scope>provided</scope>
    </dependency>

    <!--JSTL-->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>jstl</artifactId>
      <version>1.2</version>
    </dependency>

    <!--springmvc依赖-->
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-webmvc</artifactId>
      <version>4.3.16.RELEASE</version>
    </dependency>

    <!--Spring的事务-->
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-tx</artifactId>
      <version>4.3.16.RELEASE</version>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-jdbc</artifactId>
      <version>4.3.16.RELEASE</version>
    </dependency>

    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
      <version>4.3.16.RELEASE</version>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-aspects</artifactId>
      <version>4.3.16.RELEASE</version>
    </dependency>

    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-core</artifactId>
      <version>4.2.13.RELEASE</version>
    </dependency>

    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-test</artifactId>
      <version>4.3.16.RELEASE</version>
    </dependency>

    <!--jackson-->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
      <version>2.9.0</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.9.0</version>
    </dependency>
    <!--mybatis-->
    <dependency>
      <groupId>org.mybatis</groupId>
      <artifactId>mybatis-spring</artifactId>
      <version>1.3.1</version>
    </dependency>
    <dependency>
      <groupId>org.mybatis</groupId>
      <artifactId>mybatis</artifactId>
      <version>3.4.5</version>
    </dependency>
    <!--mysql驱动-->
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>5.1.9</version>
    </dependency>
    <!--连接池-->
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid</artifactId>
      <version>1.1.12</version>
    </dependency>

    <!--poi-->
    <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi</artifactId>
      <!--当前POI版本支持2003版本的Excel文件-->
      <!--<version>3.15</version>-->
      <version>4.1.2</version>
    </dependency>

    <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi-ooxml</artifactId>
      <!--当前POI版本支持2003版本及以上的Excel文件-->
      <version>4.1.2</version>
    </dependency>

    <!-- 文件上传 -->
    <dependency>
      <groupId>commons-fileupload</groupId>
      <artifactId>commons-fileupload</artifactId>
      <version>1.3.1</version>
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.12</version>
    </dependency>

    <!--        <dependency>-->
    <!--            <groupId>org.slf4j</groupId>-->
    <!--            <artifactId>slf4j-log4j12</artifactId>-->
    <!--            <version>1.7.25</version>-->
    <!--        </dependency>-->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.25</version>
    </dependency>

    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.25</version>
    </dependency>

    <!--字符串或数组或集合操作的工具类-->
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.9</version>
    </dependency>

</dependencies>

<build>
    <plugins>
      <!-- 编码和编译和JDK版本 -->
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.1</version>
        <configuration>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
      </plugin>
    </plugins>
</build>
```


### web.xml
1. 加载SpringMVC的前端控制器
   * 加载 `applicationContext-web.xml` 配置文件
2. 加载Spring容器的监听器
   * 加载 `applicationContext-service.xml` 配置文件
3. 中文乱码过滤器
```xml
  <!--注册spring的监听器-->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:spring/applicationContext-service.xml</param-value>
  </context-param>
  
  <!--注册字符集过滤器-->
  <filter>
    <filter-name>characterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
      <param-name>encoding</param-name>
      <param-value>utf-8</param-value>
    </init-param>
    <init-param>
      <param-name>forceRequestEncoding</param-name>
      <param-value>true</param-value>
    </init-param>
    <init-param>
      <param-name>forceResponseEncoding</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>characterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

  <!--

    ServletContextListener是web组件，实例化它的是tomcat
    ContextLoader是spring容器的组件，用来初始化容器
  
  -->
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>

  <!--注册springmvc的中央调度器-->
  <servlet>
    <servlet-name>dispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring/applicationContext-web.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>dispatcherServlet</servlet-name>
    <url-pattern>*.do</url-pattern>
  </servlet-mapping>
  
  <welcome-file-list>
    <welcome-file>index.html</welcome-file>
  </welcome-file-list>
```


### applicationContext-web.xml
1. 注解扫描,扫描`controller`包下的注解
2. 注解驱动,加载处理器映射器和处理器适配器的
3. 视图解析器,通过控制器的返回值字符串内容,来找到我们的jsp的具体的页面位置
    * 前缀 `/WEB-INF/jsp`
    * 后缀 `.jsp`
4. 拦截器,权限控制
    * 放行 `登录操作和跳转登录页面操作`
    * 剩下的全部拦截,必须登录后才可以访问
5. 文件上传解析器
    * 处理文件上传的配置内容
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <!--SpringMVC配置文件-->
    <!--声明组件扫描器-->
    <context:component-scan base-package="com.bjpowernode.crm.settings.web.controller"/>
    <context:component-scan base-package="com.bjpowernode.crm.workbench.web.controller"/>
    <context:component-scan base-package="com.bjpowernode.crm.exception"/>

    <!--<context:component-scan base-package="com.bjpowernode.crm"/>-->
    <!--声明视图解析器-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/jsp" />
        <property name="suffix" value=".jsp" />
    </bean>

    <!--声明注解驱动-->
    <mvc:annotation-driven/>

    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <mvc:exclude-mapping path="/settings/user/login.do"/>
            <mvc:exclude-mapping path="/settings/user/toLogin.do"/>
            <bean class="com.bjpowernode.crm.interceptor.LoginInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>

    <!-- 配置文件上传解析器 id:必须是multipartResolver-->
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="maxUploadSize" value="#{1024*1024*5}"/>
        <property name="defaultEncoding" value="utf-8"/>
    </bean>

</beans>
```


### applicationContext-service.xml
1. 注解扫描,扫描`service`包下的注解
2. 注入事务管理器,通过声明式事务控制来管理Spring的事务控制
3. 声明Spring的声明式事务控制的切面
   * 通过service包下的方法名称进行事务控制
      * save/update/delete方法开头的我们进行开启事务
4. 声明Spring的切入点,拦截service包下的所有方法,通过匹配方法的开头的名称,进行事务控制
5. 引入 `applicationContext-dao.xml` 配置文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd
">

    <!--Spring配置文件-->
    <import resource="applicationContext-dao.xml"/>
    
    <!--声明组件扫描器-->
    <context:component-scan base-package="com.bjpowernode.crm.settings.service" />
    <context:component-scan base-package="com.bjpowernode.crm.workbench.service" />

    <!--事务配置  声明式事务：在源代码之外，控制事务  声明事务管理器，指定完成事务的具体实体类 commit, rollback-->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <!--指定数据源DataSource-->
        <property name="dataSource" ref="dataSource" />
    </bean>
    <!--配置通知（切面）：指定方法使用的事务属性  id:自定义的通知名称  transaction-manager：事务管理器 -->
    <tx:advice id="myAdvice" transaction-manager="transactionManager">
        <tx:attributes>
            <!--指定方法的事务属性    name:方法名称  isolation：隔离级别  propagation：传播行为
                rollback-for：回滚的异常类， 对于自定义的异常要使用全限定名称，系统的异常类可以名称
            -->
            <tx:method name="save*" isolation="DEFAULT" propagation="REQUIRED"
                       rollback-for="com.bjpowernode.crm.exception.AjaxRequestException,
                       com.bjpowernode.crm.exception.TraditionRequestException" />
            <tx:method name="update*" isolation="DEFAULT" propagation="REQUIRED"
                       rollback-for="com.bjpowernode.crm.exception.AjaxRequestException,
                       com.bjpowernode.crm.exception.TraditionRequestException" />
            <tx:method name="delete*" isolation="DEFAULT" propagation="REQUIRED"
                       rollback-for="com.bjpowernode.crm.exception.AjaxRequestException,
                       com.bjpowernode.crm.exception.TraditionRequestException" />
            <!--除了上面的方法以外的，其他方法-->
            <tx:method name="*" propagation="SUPPORTS" read-only="true" />
        </tx:attributes>
    </tx:advice>
    <!--配置aop-->
    <aop:config>
        <!--指定切入点-->
        <aop:pointcut id="servicePt" expression="execution(* *..service..*.*(..))" />
        <!--配置增强器对象（通知+切入点）-->
        <aop:advisor advice-ref="myAdvice" pointcut-ref="servicePt" />
    </aop:config>

</beans>
```

### applicationContext-dao.xml
1. 声明数据源连接池
   * 数据库驱动,url,用户名,密码...
2. Spring整合Mybatis的配置信息
   * SqlSessionFactoryBean的注入,可以配置加载mybatis的配置文件信息
   * Mapper的扫描器,可以配置扫描dao包的所有接口,创建代理对象交给Spring容器进行管理
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd
">

    <!--Spring配置文件-->
    <!--指定数据库属性配置文件-->
    <context:property-placeholder location="classpath:properties/jdbc.properties" />

    <!--声明DataSource-->
    <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <property name="driverClassName" value="${jdbc.driver}" />
        <property name="url" value="${jdbc.url}" />
        <property name="username" value="${jdbc.username}" />
        <property name="password" value="${jdbc.password}" />
    </bean>

    <!--声明SqlSessionFactoryBean-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="configLocation" value="classpath:mybatis/mybatis-config.xml" />
    </bean>

    <!--声明MyBatis的扫描器-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
        <property name="basePackage" value="com.bjpowernode.crm.settings.dao,com.bjpowernode.crm.workbench.dao" />
    </bean>

</beans>
```


### mybatis-config.xml
1. sql日志的输出
2. 实体类的别名扫描
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

   <settings>
      <setting name="logImpl" value="STDOUT_LOGGING"/>
   </settings>
   <!--配置别名-->
   <typeAliases>
      <package name="com.bjpowernode.crm.settings.domain"/>
      <package name="com.bjpowernode.crm.workbench.domain"/>
   </typeAliases>

   <!--配置sql映射文件-->
   <mappers>
      <package name="com.bjpowernode.crm.settings.dao"/>
      <package name="com.bjpowernode.crm.workbench.dao"/>
   </mappers>
   
</configuration>
```


### jdbc.properties
> 资料中已经给大家提供了crm.sql,需要大家先行创建一个crm数据库,再执行sql文件中的语句
```properties
jdbc.driver=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/crm?characterEncoding=UTF-8
jdbc.username=root
jdbc.password=root
```


### log4j.properties
```properties
log4j.rootLogger=DEBUG,Console
log4j.appender.Console=org.apache.log4j.ConsoleAppender
log4j.appender.Console.layout=org.apache.log4j.PatternLayout
log4j.appender.Console.layout.ConversionPattern=%d [%t] %-5p [%c] - %m%n
log4j.logger.org.apache=INFO
```

## Crm模块介绍
1. 系统设置 `settings`
   * 用户模块 `user`
      * 登录操作 `login.do`
      * 跳转到登录页面操作 `toLogin.do`
   * 字典模块 `dictionary`
2. 工作台 `workbench`
   * 市场活动模块 `activity`
   * 线索模块 `clue`
   * 交易模块 `transaction`
   * 统计图表模块 `chart`

## 将html页面修改为jsp页面
1. 替换html页面 `<head>` 及以上的标签内容
   * basePath `http://localhost:8080/crm/`
      * request.getScheme() `http`
      * "://" 
      * request.getServerName() `localhost` `127.0.0.1`
      * ":" 
      * request.getServerPort() `8080`
      * request.getContextPath() `/crm`
      * "/"
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<!DOCTYPE html>
<html>
<head>
	<base href="<%=basePath%>">
```

2. 将页面中的 `../` 替换为空字符串
   * ctrl + r 进行全部替换
   * 因为我们的 `<base>` 标签包含了我们的前缀路径

3. 将 `.html` 后缀名,替换为 `.jsp`


## 封装ajax工具类
```javascript
/*
get方式以地址栏拼接键值对的方式传递数据
 */
function get(url,data,successCallBack,errorCallBack) {
    $.ajax({
        url:url,
        data:data,
        type:"get",
        contentType: "application/x-www-form-urlencoded", //默认是以表单形式传递
        dataType: "json",//服务器预计返回的参数类型
        success:successCallBack,
        error:errorCallBack
    })
}


/*
post方式传递json数据
 */
function post(url,data,successCallBack,errorCallBack) {
    $.ajax({
        url:url,
        data:JSON.stringify(data),
        type:"post",
        contentType: "application/json;charset=UTF-8", //前端以json的方式来传递数据
        dataType: "json",//服务器预计返回的参数类型
        success:successCallBack,
        error:errorCallBack
    })
}


/*
post方式提交表单参数
 */
function post4m(url,data,successCallBack,errorCallBack) {
    $.ajax({
        url:url,
        data:data,
        type:"post",
        contentType: "application/x-www-form-urlencoded", //默认是以表单形式传递
        dataType: "json",//服务器预计返回的参数类型
        success:successCallBack,
        error:errorCallBack
    })
}
```