package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorL extends AbstractProcessor<String> {

    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        String aResult = (String) vertex.getDependencyData().get("H");
        int cResult = (int) vertex.getDependencyData().get("J");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture("{" + aResult + cResult + ": L}");
    }
}
