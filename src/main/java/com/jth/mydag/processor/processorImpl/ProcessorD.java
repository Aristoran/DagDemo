package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorD extends AbstractProcessor<List<Integer>> {
    @Override
    public CompletableFuture<List<Integer>> process(Vertex<List<Integer>> vertex) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Integer cRes = (Integer)vertex.getDependencyData().get("C");
        return CompletableFuture.completedFuture(Collections.singletonList(cRes));
    }
}
