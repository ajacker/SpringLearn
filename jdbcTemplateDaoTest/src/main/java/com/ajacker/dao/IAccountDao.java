package com.ajacker.dao;

import com.ajacker.domain.Account;

/**
 * @author ajacker
 * 账户的持久层接口
 */
public interface IAccountDao {

    /**
     * 根据id查询账户
     * @param id
     * @return
     */
    Account findAccountById(int id);

    /**
     * 根据姓名查询账户
     * @param name
     * @return
     */
    Account findAccountByName(String name);

    /**
     * 更新账户
     * @param account
     */
    void updateAccount(Account account);
}
