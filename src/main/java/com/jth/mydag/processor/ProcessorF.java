package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorF extends AbstractProcessor<String> {
    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Integer> dResult = (List<Integer>) vertex.getDependencyData().get("D");
        String eResult = (String) vertex.getDependencyData().get("E");
        return CompletableFuture.completedFuture(dResult.get(0) + eResult);
    }
}
