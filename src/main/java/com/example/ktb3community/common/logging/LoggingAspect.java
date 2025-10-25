package com.example.ktb3community.common.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution(* com.example.ktb3community.auth.service.AuthService.signup(..))")
    public void serviceLayer() {}

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("[LOG Before] " + methodName + " 메서드 실행 시작 ");
    }

    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("[LOG After] " + methodName + " 메서드 실행 종료 ");
    }

    @AfterThrowing("serviceLayer()")
    public void logAfterThrowing(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.err.println("[LOG After Throwing] " + methodName + " 메서드 실행 중 예외 발생 ");
    }
}
