package com.ajacker.test;

import com.ajacker.domain.Account;
import com.ajacker.service.IAccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * 使用junit单元测试配置
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:bean.xml")
public class AccountServiceTest {
    @Resource(name = "accountService")
    private IAccountService as;

    @Test
    public void testFindAll() {
        List<Account> accounts = as.findAllAccount();
        accounts.forEach(System.out::println);
    }

    @Test
    public void testTransfer() {
        as.transfer("ccc", "test", 100f);
    }
}
