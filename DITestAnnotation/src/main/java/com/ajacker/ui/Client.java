package com.ajacker.ui;

import com.ajacker.service.IAccountService;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ajacker
 * 模拟一个表现层
 */
public class Client {
    public static void main(String[] args) {
        //获取核心容器对象
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //根据id获取对象
        IAccountService as = ac.getBean("accountService", IAccountService.class);
        IAccountService as2 = ac.getBean("accountService", IAccountService.class);
        System.out.println("是否为单例："+as.equals(as2));
        as.saveAccount();
        ac.close();
    }
}
