package com.ajacker.utils;


import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author ajacker
 * 记录日志
 */
public class Logger {
    public void printLogBefore(){
        System.out.println("Logger类中的printLogBefore开始记录日志...");
    }
    public void printLogAfterReturning(){
        System.out.println("Logger类中的printLogAfterReturning开始记录日志...");
    }
    public void printLogAfterThrowing(){
        System.out.println("Logger类中的printLogAfterThrowing开始记录日志...");
    }
    public void printLogAfter(){
        System.out.println("Logger类中的printLogAfter开始记录日志...");
    }

    public Object printLogAround(ProceedingJoinPoint joinPoint) {
        Object rtValue;
        try {
            Object[] args = joinPoint.getArgs();
            System.out.println("Logger类中的printLogAround开始记录日志...前置");
            rtValue = joinPoint.proceed(args);
            System.out.println("Logger类中的printLogAround开始记录日志...后置");
            return rtValue;
        } catch (Throwable throwable) {
            System.out.println("Logger类中的printLogAround开始记录日志...异常");
            throw new RuntimeException(throwable);
        }finally {
            System.out.println("Logger类中的printLogAround开始记录日志...最终");
        }
    }
}
