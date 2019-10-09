package com.ajacker.test;

import com.ajacker.domain.Account;
import com.ajacker.service.IAccountService;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * 使用junit单元测试配置
 */
public class AccountServiceTest {
    @Test
    public void testFindAll() {
        //1.获取容器
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取业务层对象
        IAccountService as = ac.getBean("accountService",IAccountService.class);
        //3.执行方法
        List<Account> accounts = as.findAllAccount();
        accounts.forEach(System.out::println);
    }
    @Test
    public void testFindOne() {
        //1.获取容器
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取业务层对象
        IAccountService as = ac.getBean("accountService",IAccountService.class);
        //3.执行方法
        Account account = as.findAccountById(1);
        System.out.println(account);
    }
    @Test
    public void testSave() {
        //1.获取容器
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取业务层对象
        IAccountService as = ac.getBean("accountService",IAccountService.class);
        //3.执行方法
        Account account = new Account();
        account.setName("test");
        account.setMoney(1234f);
        as.saveAccount(account);
        System.out.println(account);
    }
    @Test
    public void testUpdate() {
        //1.获取容器
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取业务层对象
        IAccountService as = ac.getBean("accountService",IAccountService.class);
        //3.执行方法
        Account account = as.findAccountById(1);
        account.setMoney(555f);
        as.updateAccount(account);
        System.out.println(account);
    }
    @Test
    public void testDelete() {
        //1.获取容器
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取业务层对象
        IAccountService as = ac.getBean("accountService",IAccountService.class);
        //3.执行方法
        as.deleteAccount(3);
    }
}
