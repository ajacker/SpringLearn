package com.ajacker.ui;

import com.ajacker.service.IAccountService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ajacker
 * 模拟一个表现层
 */
public class Client {
    public static void main(String[] args) {
        //获取核心容器对象
        ApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //根据id获取对象
        IAccountService as = ac.getBean("accountService3", IAccountService.class);
        as.saveAccount();
    }
}
