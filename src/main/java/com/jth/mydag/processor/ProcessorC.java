package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorC implements AbstractProcessor<Integer>{
    @Override
    public CompletableFuture<Integer> process(Vertex<Integer> vertex) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(1024);
    }
}
