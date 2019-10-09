package com.ajacker.dao.impl;

import com.ajacker.dao.IAccountDao;
import com.ajacker.domain.Account;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;

/**
 * @author ajacker
 */
public class AccountDaoImpl implements IAccountDao {
    private QueryRunner runner;

    public void setRunner(QueryRunner runner) {
        this.runner = runner;
    }

    @Override
    public List<Account> findAllAccount() {
        try {
            return runner.query("select * from account", new BeanListHandler<>(Account.class));
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public Account findAccountById(Integer id) {
        try {
            return runner.query("select * from account where id = ?", new BeanHandler<>(Account.class),id);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void saveAccount(Account account) {
        try {
            runner.update("insert into account(name,money) values(?,?)",account.getName(),account.getMoney());
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void updateAccount(Account account) {
        try {
            runner.update("update account set name=?,money=? where id=?",account.getName(),account.getMoney(),account.getId());
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void deleteAccount(Integer id) {
        try {
            runner.update("delete from account where id=?",id);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

}
