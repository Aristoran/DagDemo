package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorJ extends AbstractProcessor<Integer> {

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
