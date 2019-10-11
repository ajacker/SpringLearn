package com.ajacker.jdbcTemplate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author ajacker
 */
public class JdbcDemo1 {
    public static void main(String[] args) {
        //准备数据源
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Shanghai&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("456852");
        //创建对象
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        //执行操作
        jt.execute("insert into account(name, money) values('ccc',1000)");
    }
}
