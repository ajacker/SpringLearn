package com.ajacker.service.impl;


import com.ajacker.service.IAccountService;

import java.util.Date;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
public class AccountServiceImpl1 implements IAccountService {
    /**
     * 如果数据经常变化 则不适合用配置文件注入
     */
    private String name;
    private Integer age;
    private Date birthday;

    public AccountServiceImpl1(String name, Integer age, Date birthday) {
        this.name = name;
        this.age = age;
        this.birthday = birthday;
    }

    @Override
    public void saveAccount() {
        System.out.println("service中的saveAccount被执行了。。。"+name+","+age+","+birthday);
    }
}
