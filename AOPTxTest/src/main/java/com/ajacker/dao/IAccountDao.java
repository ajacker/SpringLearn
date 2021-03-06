package com.ajacker.dao;


import com.ajacker.domain.Account;

import java.util.List;

/**
 * @author ajacker
 * 账户的持久层接口
 */
public interface IAccountDao {
    /**
     * 查询所有
     * @return
     */
    List<Account> findAllAccount();

    /**
     * 查询一个
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
     * 通过名称查询账户
     * @param name 名称
     * @return 如果无结果，返回null
     * @throws RuntimeException 结果超过一个
     */
    Account findAccountByName(String name);
}
