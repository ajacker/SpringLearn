package com.ajacker.factorytest.ui;

import com.ajacker.factorytest.factory.BeanFactory;
import com.ajacker.factorytest.service.IAccountService;

/**
 * @author ajacker
 * 模拟一个表现层
 */
public class Client {
    public static void main(String[] args) {
        for (int i = 0; i < 4; i++) {
            IAccountService as = (IAccountService) BeanFactory.getBean("accountService");
            System.out.println(as);
        }

//        as.saveAccount();
    }
}
