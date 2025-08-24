package kr.hhplus.be.server.common.functional;

@FunctionalInterface
public interface CustomThrowingSupplier<T> {

    T get() throws Throwable;
}
