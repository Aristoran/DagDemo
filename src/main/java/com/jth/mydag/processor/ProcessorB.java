package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorB extends AbstractProcessor<String> {

    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String aResult = (String)vertex.getDependencyData().get("A");
        return CompletableFuture.completedFuture(aResult)
                .thenApply(r -> "1024");
    }
}
