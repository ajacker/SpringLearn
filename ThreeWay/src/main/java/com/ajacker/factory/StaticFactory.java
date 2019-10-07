package com.ajacker.factory;

import com.ajacker.service.IAccountService;
import com.ajacker.service.impl.AccountServiceImpl;

/**
 * @author ajacker
 * 模拟一个静态工厂类
 */
public class StaticFactory {
    public static IAccountService getAccountService(){
        return new AccountServiceImpl();
    }
}
