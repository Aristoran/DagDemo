package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 * @Description: 抽象接口，提供执行方法
 */
public interface AbstractProcessor<T> {

    CompletableFuture<T> process(Vertex<T> vertex);

    default void reset() {

    };
}
