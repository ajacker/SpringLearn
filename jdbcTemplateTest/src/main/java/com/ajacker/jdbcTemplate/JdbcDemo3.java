package com.ajacker.jdbcTemplate;

import com.ajacker.domain.Account;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author ajacker
 */
public class JdbcDemo3 {
    public static void main(String[] args) {
        //1.获取容器
        AbstractApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取对象
        JdbcTemplate jt = ac.getBean("jdbcTemplate",JdbcTemplate.class);
        //3.执行操作
        //保存
        jt.update("insert into account(name, money) values(?,?)","eee",3333f);
        //更新
        jt.update("update account set money=? where name=?",6666f,"eee");
        //删除
        jt.update("delete from account where name=?","aaa");
        //查询所有
        //List<Account> accounts = jt.query("select * from account where money > ?",new AccountRowMapper(),100f);
        List<Account> accounts = jt.query("select * from account where money > ?",new BeanPropertyRowMapper<>(Account.class),100f);
        accounts.forEach(System.out::println);
        //查询一个
        List<Account> accountList = jt.query("select * from account where money = ?",new BeanPropertyRowMapper<>(Account.class),666f);
        System.out.println(accountList.isEmpty()?"无":accountList.get(0));
        //查询返回一行一列
        Integer i = jt.queryForObject("select count(*) from account where money > ?",Integer.class,100f);
        System.out.println(i);

    }

}

/**
 * 定义Account的封装策略
 */
class AccountRowMapper implements RowMapper<Account>{

    /**
     * 把结果集中的数据封装到Account
     * @param resultSet
     * @param i
     * @return
     * @throws SQLException
     */
    @Override
    public Account mapRow(ResultSet resultSet, int i) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getInt("id"));
        account.setName(resultSet.getString("name"));
        account.setMoney(resultSet.getFloat("money"));
        return account;
    }
}