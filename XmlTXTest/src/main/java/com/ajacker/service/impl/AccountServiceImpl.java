package com.ajacker.service.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.domain.Account;
import com.ajacker.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ajacker
 * 业务层实现类
 */
@Service("accountService")
public class AccountServiceImpl implements IAccountService {
    private final IAccountDao accountDao;

    public AccountServiceImpl(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account findAccountById(Integer accountId) {
        return accountDao.findAccountById(accountId);
    }

    public void transfer(String sourceName, String targetName, Float money) {
        System.out.println("start transfer");
        // 1.根据名称查询转出账户
        Account source = accountDao.findAccountByName(sourceName);
        // 2.根据名称查询转入账户
        Account target = accountDao.findAccountByName(targetName);
        // 3.转出账户减钱
        source.setMoney(source.getMoney() - money);
        // 4.转入账户加钱
        target.setMoney(target.getMoney() + money);
        // 5.更新转出账户
        accountDao.updateAccount(source);
        // 6.更新转入账户
        accountDao.updateAccount(target);
    }
}
