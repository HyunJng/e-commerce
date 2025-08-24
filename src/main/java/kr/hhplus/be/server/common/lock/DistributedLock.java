package kr.hhplus.be.server.common.lock;

import kr.hhplus.be.server.common.lock.resolver.LockKeyResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    Class<? extends LockKeyResolver> resolver();

    long waitTime() default 5L;

    long leaseTime() default 3L;
}
