package kr.hhplus.be.server.common.lock;

@FunctionalInterface
public interface CustomThrowingSupplier<T> {

    T get() throws Throwable;
}
