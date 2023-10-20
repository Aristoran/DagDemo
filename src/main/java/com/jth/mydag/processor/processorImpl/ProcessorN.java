package com.jth.mydag.processor.processorImpl;

import com.jth.mydag.graph.Vertex;
import com.jth.mydag.processor.AbstractProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author jiatihui
 */
public class ProcessorN extends AbstractProcessor<Map<String, Object>> {
    @Override
    public CompletableFuture<Map<String, Object>> process(Vertex<Map<String, Object>> vertex) {
        Object aResult = vertex.getDependencyData().get("H");
        Object bResult = vertex.getDependencyData().get("I");
        Object fResult = vertex.getDependencyData().get("M");
        Map<String, Object> map = new HashMap<>();
        map.put("H", aResult);
        map.put("I", bResult);
        map.put("M", fResult);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(map);
    }
}
