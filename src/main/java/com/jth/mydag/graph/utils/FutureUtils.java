package com.jth.mydag.graph.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class FutureUtils {
    public static <T> void collect(List<CompletableFuture<T>> futures) throws InterruptedException {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
