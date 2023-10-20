package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorI extends AbstractProcessor<String> {

    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String aResult = (String)vertex.getDependencyData().get("H");
        return CompletableFuture.completedFuture(aResult)
                .thenApply(r -> "1024");
    }
}
