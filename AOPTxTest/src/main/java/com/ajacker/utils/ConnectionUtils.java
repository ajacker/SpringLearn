package com.ajacker.utils;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author ajacker
 * 连接的工具类，用于从数据源中获取连接并实现和线程的绑定
 */
@Component("connectionUtils")
public class ConnectionUtils {
    private ThreadLocal<Connection> threadLocal = new ThreadLocal<>();
    private final DataSource dataSource;

    public ConnectionUtils(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getThreadConnection() {
        try {
            //1.先从ThreadLocal上获取
            Connection conn = threadLocal.get();
            //2.判断当前线程上是否有连接
            if (conn == null) {
                //3.从数据源获取连接并绑定
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                threadLocal.set(conn);
            }
            //4.返回线程上的连接
            return conn;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 连接和线程解绑
     */
    public void removeConnection(){
        threadLocal.remove();
    }
}
