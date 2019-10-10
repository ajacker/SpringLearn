package com.ajacker.factory;

import com.ajacker.service.IAccountService;
import com.ajacker.utils.TransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * @author ajacker
 * 用于创建Service的代理对象的工厂
 */
@Component
public class BeanFactory {
    private final IAccountService accountService;
    private final TransactionManager transactionManager;

    public BeanFactory(TransactionManager transactionManager, IAccountService accountService) {
        this.transactionManager = transactionManager;
        this.accountService = accountService;
    }

    @Bean("proxyAccountService")
    public IAccountService getAccountService(){
        return (IAccountService) Proxy.newProxyInstance(accountService.getClass().getClassLoader(),
                accountService.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    Object rtValue;
                    try {
                        //1.开启事务
                        transactionManager.beginTransaction();
                        //2.执行操作
                        rtValue = method.invoke(accountService, args);
                        //3.提交事务
                        transactionManager.commit();
                        //4.返回结果
                        return rtValue;
                    }catch (Exception e){
                        //5.回滚操作
                        transactionManager.rollback();
                        throw new RuntimeException(e);
                    }finally {
                        //6.释放连接
                        transactionManager.release();
                    }
                });
    }
}
