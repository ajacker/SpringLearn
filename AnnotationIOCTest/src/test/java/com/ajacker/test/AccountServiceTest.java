package com.ajacker.test;

import com.ajacker.domain.Account;
import com.ajacker.service.IAccountService;
import config.SpringConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * 使用junit单元测试配置
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class AccountServiceTest {
    @Resource(name = "accountService")
    private IAccountService as;

    @Test
    public void testFindAll() {
        //3.执行方法
        List<Account> accounts = as.findAllAccount();
        accounts.forEach(System.out::println);
    }
    @Test
    public void testFindOne() {
        //3.执行方法
        Account account = as.findAccountById(1);
        System.out.println(account);
    }
    @Test
    public void testSave() {
        //3.执行方法
        Account account = new Account();
        account.setName("test");
        account.setMoney(1234f);
        as.saveAccount(account);
        System.out.println(account);
    }
    @Test
    public void testUpdate() {
        //3.执行方法
        Account account = as.findAccountById(1);
        account.setMoney(555f);
        as.updateAccount(account);
        System.out.println(account);
    }
    @Test
    public void testDelete() {
        //3.执行方法
        as.deleteAccount(3);
    }
}
