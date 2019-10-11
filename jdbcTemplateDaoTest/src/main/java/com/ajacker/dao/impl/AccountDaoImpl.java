package com.ajacker.dao.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author ajacker
 * 持久层实现类
 */
@Repository("dataSource")
public class AccountDaoImpl extends JdbcDaoSupport implements IAccountDao {

    @Autowired
    public AccountDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public Account findAccountById(int id) {
        List<Account> accounts = getJdbcTemplate().query("select * from account where id = ?", new BeanPropertyRowMapper<>(Account.class), id);
        return accounts.isEmpty()?null:accounts.get(0);
    }

    @Override
    public Account findAccountByName(String name) {
        List<Account> accounts = getJdbcTemplate().query("select * from account where name = ?", new BeanPropertyRowMapper<>(Account.class), name);
        if (accounts.isEmpty()) {
            return null;
        }
        if(accounts.size()>1){
            throw new RuntimeException("结果不止一个");
        }
        return accounts.get(0);
    }

    @Override
    public void updateAccount(Account account) {
        getJdbcTemplate().update("update account set name=?,money=? where id=?", account.getName(), account.getMoney(), account.getId());
    }
}
