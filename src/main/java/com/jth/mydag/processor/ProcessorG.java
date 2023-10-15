package com.jth.mydag.processor;

import com.jth.mydag.graph.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorG implements AbstractProcessor<Map<String, Object>>{
    @Override
    public CompletableFuture<Map<String, Object>> process(Vertex<Map<String, Object>> vertex) {
        Object aResult = vertex.getDependencyData().get("A");
        Object fResult = vertex.getDependencyData().get("F");
        Map<String, Object> map = new HashMap<>();
        map.put("A", aResult);
        map.put("F", fResult);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(map);
    }
}
