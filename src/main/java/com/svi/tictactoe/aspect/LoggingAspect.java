package com.svi.tictactoe.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs execution, duration, and outcome details for controller and service method calls.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Wraps controller and service methods to record their execution time and whether they succeed or fail.
     *
     * @param joinPoint the intercepted method invocation
     * @return the value returned by the intercepted method
     * @throws Throwable if the intercepted method fails
     */
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
