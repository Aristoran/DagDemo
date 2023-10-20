package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorM extends AbstractProcessor<String> {
    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Integer> kResult = (List<Integer>) vertex.getDependencyData().get("K");
        String lResult = (String) vertex.getDependencyData().get("L");
        String input = (String) vertex.getDependencyData().get("A");
        return CompletableFuture.completedFuture(kResult.get(0) + lResult + input);
    }
}
