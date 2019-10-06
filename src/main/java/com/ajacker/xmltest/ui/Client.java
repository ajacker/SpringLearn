package com.ajacker.xmltest.ui;

import com.ajacker.xmltest.dao.IAccountDao;
import com.ajacker.xmltest.service.IAccountService;
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
        IAccountService as = ac.getBean("accountService", IAccountService.class);
        IAccountDao adao = ac.getBean("accountDao", IAccountDao.class);
        as.saveAccount();
    }
}
