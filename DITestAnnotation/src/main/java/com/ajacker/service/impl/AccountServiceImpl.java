package com.ajacker.service.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
@Service(value = "accountService")
public class AccountServiceImpl implements IAccountService {
    @Autowired
    @Qualifier(value = "accountDao")
    private IAccountDao accountDao;



    @Override
    public void saveAccount() {
        accountDao.saveAccount();
    }
}
