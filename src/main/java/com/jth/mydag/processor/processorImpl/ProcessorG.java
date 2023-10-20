package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorG extends AbstractProcessor<Map<String, Object>> {
    @Override
    public CompletableFuture<Map<String, Object>> process(Vertex<Map<String, Object>> vertex) {
        Object aResult = vertex.getDependencyData().get("A");
        Object bResult = vertex.getDependencyData().get("B");
        Object fResult = vertex.getDependencyData().get("F");
        Object subGraphResult = vertex.getDependencyData().get("subName");
        Map<String, Object> map = new HashMap<>();
        map.put("A", aResult);
        map.put("B", bResult);
        map.put("F", fResult);
        map.put("subName", subGraphResult);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(map);
    }
}
