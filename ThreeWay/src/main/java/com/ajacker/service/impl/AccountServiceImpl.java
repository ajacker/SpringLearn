package com.ajacker.service.impl;

import com.ajacker.service.IAccountService;
/**
 * @author ajacker
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService {

    @Override
    public void saveAccount() {
        System.out.println("保存了账户");
    }
}
