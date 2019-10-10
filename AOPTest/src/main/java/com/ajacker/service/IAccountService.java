package com.ajacker.service;

import com.ajacker.domain.Account;

import java.util.List;

/**
 * @author ajacker
 * 账户的业务层接口
 */
public interface IAccountService {

    /**
     * 查询所有
     * @return
     */
    List<Account> findAllAccount();

    /**
     * 查询一个
     * @param id
     * @return
     */
    Account findAccountById(Integer id);

    /**
     *  保存操作
     * @param account
     */
    void saveAccount(Account account);

    /**
     * 更新
     * @param account
     */
    void updateAccount(Account account);

    /**
     * 删除
     * @param id
     */
    void deleteAccount(Integer id);

    /**
     * 转账
     * @param sourceName 转出账户名
     * @param targetName 转入账户名
     * @param money      金额
     */
    void transfer(String sourceName,String targetName,float money);
}
