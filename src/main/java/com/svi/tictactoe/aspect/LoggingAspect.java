package com.svi.tictactoe.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.svi.tictactoe.controller..*(..)) || execution(* com.svi.tictactoe.service..*(..))" )
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        long startTime = System.currentTimeMillis();
        LOGGER.info("Executing {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("Completed {}.{} in {} ms", className, methodName, duration);
            return result;

        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("Failed {}.{} after {} ms: {}", className, methodName, duration, exception.getMessage());
            throw exception;
        }
    }
}