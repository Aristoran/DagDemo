package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorD implements AbstractProcessor<List<Integer>>{
    @Override
    public CompletableFuture<List<Integer>> process(Vertex<List<Integer>> vertex) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String bRes = (String)vertex.getDependencyData().get("B");
        Integer cRes = (Integer)vertex.getDependencyData().get("C");
        return CompletableFuture.completedFuture(Arrays.asList(Integer.parseInt(bRes), cRes));
    }
}
