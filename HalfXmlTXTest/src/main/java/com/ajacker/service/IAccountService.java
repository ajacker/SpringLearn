package com.ajacker.service;

import com.ajacker.domain.Account;

/**
 * @author ajacker
 * 业务层接口
 */
public interface IAccountService {

    /**
     * 根据id查询账户信息
     * @param accountId
     * @return
     */
    Account findAccountById(Integer accountId);

    /**
     * 转账
     * @param sourceName
     * @param targetName
     * @param money
     */
    void transfer(String sourceName, String targetName, Float money);
}