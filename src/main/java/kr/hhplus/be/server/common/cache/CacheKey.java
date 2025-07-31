package kr.hhplus.be.server.common.cache;

public final class CacheKey {
    private CacheKey() {}

    public static String bestProductsKey() {
        return "best-products:list";
    }

}