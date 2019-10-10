package com.ajacker.service;

/**
 * @author ajacker
 * 账户的业务层接口
 */
public interface IAccountService {

    /**
     * 模拟保存账户
     */
    void saveAccount();

    /**
     * 更新账户
     * @param i
     */
    void updateAccount(int i);


    /**
     * 删除账户
     * @return
     */
    int deleteAccount();
}
