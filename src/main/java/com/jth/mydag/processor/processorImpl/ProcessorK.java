package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorK extends AbstractProcessor<List<Integer>> {

    @Override
    public CompletableFuture<List<Integer>> process(Vertex<List<Integer>> vertex) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Integer cRes = (Integer)vertex.getDependencyData().get("J");
        return CompletableFuture.completedFuture(Collections.singletonList(cRes));
    }
}
