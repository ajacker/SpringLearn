package com.ajacker.service.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.domain.Account;
import com.ajacker.service.IAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ajacker
 * 账户的业务层实现类
 */
@Service("accountService")
public class AccountServiceImpl implements IAccountService {

    @Resource(name = "accountDao")
    private IAccountDao accountDao;


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

    @Override
    public void transfer(String sourceName, String targetName, float money) {
        //2.1根据名称查询转出账户
        Account sourceAccount = accountDao.findAccountByName(sourceName);
        //2.2根据名称查询转入账户
        Account targetAccount = accountDao.findAccountByName(targetName);
        //2.3转出账户减钱
        sourceAccount.setMoney(sourceAccount.getMoney() - money);
        //2.4转入账户加钱
        targetAccount.setMoney(targetAccount.getMoney() + money);
        //2.5更新转出账户
        updateAccount(sourceAccount);
        //2.6更新转入账户
        updateAccount(targetAccount);
    }
}
