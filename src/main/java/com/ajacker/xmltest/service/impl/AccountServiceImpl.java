package com.ajacker.xmltest.service.impl;

import com.ajacker.xmltest.dao.IAccountDao;
import com.ajacker.xmltest.dao.impl.AccountDaoImpl;
import com.ajacker.xmltest.service.IAccountService;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService {
    private IAccountDao accountDao = new AccountDaoImpl();

    @Override
    public void saveAccount() {
        accountDao.saveAccount();
    }
}
