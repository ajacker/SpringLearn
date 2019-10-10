package com.ajacker.utils;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author ajacker
 * 记录日志
 */
@Component("logger")
@Aspect
public class Logger {
    @Pointcut("execution( * com.ajacker.service.impl.AccountServiceImpl.*(..))")
    private void pt(){}

    /**
     * 前置通知
     */
    @Before("pt()")
    public void printLogBefore(){
        System.out.println("Logger类中的printLogBefore开始记录日志...");
    }

    /**
     * 后置通知
     */
    @AfterReturning("pt()")
    public void printLogAfterReturning(){
        System.out.println("Logger类中的printLogAfterReturning开始记录日志...");
    }

    /**
     * 异常通知
     */
    @AfterThrowing("pt()")
    public void printLogAfterThrowing(){
        System.out.println("Logger类中的printLogAfterThrowing开始记录日志...");
    }

    /**
     * 最终通知
     */
    @After("pt()")
    public void printLogAfter(){
        System.out.println("Logger类中的printLogAfter开始记录日志...");
    }

    /**
     * 环绕通知
     */
    @Around("pt()")
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
