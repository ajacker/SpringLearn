package com.ajacker.service.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
@Service(value = "accountService")
@Scope(value = "prototype")
public class AccountServiceImpl implements IAccountService {
    @Autowired
    @Qualifier(value = "accountDao")
    private IAccountDao accountDao;

    @PostConstruct
    public void init(){
        System.out.println("初始化方法调用");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁方法调用");
    }

    @Override
    public void saveAccount() {
        accountDao.saveAccount();
    }
}
