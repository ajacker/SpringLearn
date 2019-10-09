package com.ajacker.service.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.dao.impl.AccountDaoImpl;
import com.ajacker.domain.Account;
import com.ajacker.service.IAccountService;
import org.apache.commons.dbutils.QueryRunner;

import java.util.List;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService {
    private IAccountDao accountDao;

    public void setAccountDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public List<Account> findAllAccount() {
        return accountDao.findAllAccount();
    }

    @Override
    public Account findAccountById(Integer id) {
        return accountDao.findAccountById(id);
    }

    @Override
    public void saveAccount(Account account) {
        accountDao.saveAccount(account);
    }

    @Override
    public void updateAccount(Account account) {
        accountDao.updateAccount(account);
    }

    @Override
    public void deleteAccount(Integer id) {
        accountDao.deleteAccount(id);
    }

}
