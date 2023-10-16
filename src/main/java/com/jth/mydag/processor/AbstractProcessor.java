package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public abstract class AbstractProcessor<T> implements IProcessor<T> {
    @Override
    public CompletableFuture<T> process(Vertex<T> vertex) {
        // TODO: Implement processing logic here
        return null;
    }
    public void reset() {
        // TODO: Implement reset logic here
    }
}
