package com.ajacker.dao;



import com.ajacker.domain.Account;

/**
 * @author ajacker
 * 账户的持久层接口
 */
public interface IAccountDao {

    /**
     * 查询一个
     * @param id
     * @return
     */
    Account findAccountById(Integer id);

    /**
     * 更新
     * @param account
     */
    void updateAccount(Account account);

    /**
     * 通过名称查询账户
     * @param name 名称
     * @return 如果无结果，返回null
     * @throws RuntimeException 结果超过一个
     */
    Account findAccountByName(String name);
}
