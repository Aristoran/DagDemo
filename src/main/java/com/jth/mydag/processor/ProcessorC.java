package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorC extends AbstractProcessor<Integer> {
    @Override
    public CompletableFuture<Integer> process(Vertex<Integer> vertex) {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(1024);
    }
}
