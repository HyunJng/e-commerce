package kr.hhplus.be.server.common.lock;

import org.aspectj.lang.ProceedingJoinPoint;

import java.util.List;

public interface LockKeyResolver {

    List<String> resolve(ProceedingJoinPoint joinPoint);

}
