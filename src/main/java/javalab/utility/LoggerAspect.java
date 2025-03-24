package javalab.utility;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerAspect {

    Logger logger = Logger.getLogger(getClass().getName());

    @Before("execution(* javalab..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.log(Level.FINEST, "Executing: {0}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "execution(* javalab..*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.log(Level.FINEST, "Executed: {0} with result: {1}",
                new Object[]{joinPoint.getSignature().toShortString(), result});
    }

    @AfterThrowing(pointcut = "execution(* javalab..*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        logger.log(Level.SEVERE, "Exception in: {0} with cause: {1}",
                new Object[]{joinPoint.getSignature().toShortString(), error.getMessage()});
    }
}