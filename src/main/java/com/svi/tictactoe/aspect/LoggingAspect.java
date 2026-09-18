package com.svi.tictactoe.aspect;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Logs request, response, duration, and failure details for controller method calls.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);
    /**
     * Logs controller requests and their outcomes.
     *
     * @param joinPoint the intercepted controller method invocation
     * @return the value returned by the intercepted method
     * @throws Throwable if the intercepted method fails
     */
    @Around("execution(* com.svi.tictactoe.controller..*(..))")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        long startTime = System.nanoTime();
        LOGGER.debug("Request: {}.{} args={}", className, methodName, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = getDurationInMilliseconds(startTime);
            if (result instanceof ResponseEntity<?> response) {
                LOGGER.debug("Response: {}.{} status={} duration={}ms", className, methodName, response.getStatusCode(), duration);
            } else {
                LOGGER.debug("Response: {}.{} duration={}ms", className, methodName, duration);
            }

            return result;

        } catch (Exception exception) {
            long duration = getDurationInMilliseconds(startTime);
            LOGGER.warn("Request failed: {}.{} exception={} message=\"{}\" duration={}ms", className, methodName, exception.getClass().getSimpleName(), exception.getMessage(), duration);
            throw exception;
        }
    }

    private long getDurationInMilliseconds(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }
}