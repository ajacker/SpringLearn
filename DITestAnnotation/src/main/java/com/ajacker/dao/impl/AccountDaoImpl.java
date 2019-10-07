package com.ajacker.dao.impl;


import com.ajacker.dao.IAccountDao;
import org.springframework.stereotype.Repository;

/**
 * @author ajacker
 * 账户的持久层实现类
 */
@Repository(value = "accountDao")
public class AccountDaoImpl implements IAccountDao {
    @Override
    public void saveAccount() {
        System.out.println("保存了账户");
    }
}
