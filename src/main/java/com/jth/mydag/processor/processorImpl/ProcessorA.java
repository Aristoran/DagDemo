package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */

public class ProcessorA extends AbstractProcessor<String> {

    @Override
    public CompletableFuture<String> process(Vertex<String> vertex) {
        return CompletableFuture.completedFuture("A" + System.currentTimeMillis());
    }
}
