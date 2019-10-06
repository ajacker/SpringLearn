package com.ajacker.factorytest.service.impl;

import com.ajacker.factorytest.dao.IAccountDao;
import com.ajacker.factorytest.factory.BeanFactory;
import com.ajacker.factorytest.service.IAccountService;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService {
    private IAccountDao accountDao = (IAccountDao) BeanFactory.getBean("accountDao");

    @Override
    public void saveAccount() {
        accountDao.saveAccount();
    }
}
