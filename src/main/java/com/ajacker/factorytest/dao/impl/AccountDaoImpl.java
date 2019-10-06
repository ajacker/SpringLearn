package com.ajacker.factorytest.dao.impl;

import com.ajacker.factorytest.dao.IAccountDao;

/**
 * @author ajacker
 * 账户的持久层实现类
 */
public class AccountDaoImpl implements IAccountDao {
    @Override
    public void saveAccount() {
        System.out.println("保存了账户");
    }
}
