package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorE extends AbstractProcessor<String> {
    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        String aResult = (String) vertex.getDependencyData().get("A");
        int cResult = (int) vertex.getDependencyData().get("C");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture("{" + aResult + cResult + ": E}");
    }
}
