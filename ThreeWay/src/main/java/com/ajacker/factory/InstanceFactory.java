package com.ajacker.factory;

import com.ajacker.service.IAccountService;
import com.ajacker.service.impl.AccountServiceImpl;

/**
 * @author ajacker
 * 模拟一个无法修改的用于创建对象的类
 */
public class InstanceFactory {
    public IAccountService getAccountService(){
        return new AccountServiceImpl();
    }
}
