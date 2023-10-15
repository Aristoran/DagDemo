package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */

public class ProcessorA implements AbstractProcessor<String> {

    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        return CompletableFuture.completedFuture("A");
    }

}
