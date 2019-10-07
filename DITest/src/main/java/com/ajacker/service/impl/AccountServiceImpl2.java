package com.ajacker.service.impl;


import com.ajacker.service.IAccountService;

import java.util.Date;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
public class AccountServiceImpl2 implements IAccountService {
    /**
     * 如果数据经常变化 则不适合用配置文件注入
     */
    private String name;
    private Integer age;
    private Date birthday;

    public void setUserName(String name) {
        this.name = name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public void saveAccount() {
        System.out.println("service中的saveAccount被执行了。。。"+name+","+age+","+birthday);
    }
}
