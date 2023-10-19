package com.jth.mydag.graph;


import com.jth.mydag.graph.config.AppConfig;
import com.jth.mydag.graph.scheduler.AbstractSubScheduler;
import com.jth.mydag.graph.scheduler.ParallelScheduler;
import com.jth.mydag.processor.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GraphTest {

    Graph<?> graph;
    AbstractSubScheduler<?> scheduler;
    @Before
    public void init() {
        List<Vertex<?>> vertices = initVertices();
        scheduler = new ParallelScheduler<>(new AppConfig().executorService(),
                new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>(),
                ParallelScheduler.RunStrategy.POOL);
        graph = new Graph<>(vertices, scheduler);
    }

    @Test
    public void test() {
        graph.run();
        System.out.println(graph.getContext().getResult());
    }

    @After
    public void shutdown() {
        ((ParallelScheduler<?>) scheduler).executorService.shutdown();
    }

    private List<Vertex<?>> initVertices() {
        Vertex<String> vertexA = new Vertex<>("A", "123", new ProcessorA());
        Vertex<String> vertexB = new Vertex<>("B", "234", new ProcessorB());
        Vertex<Integer> vertexC = new Vertex<>("C", "345", new ProcessorC());
        Vertex<List<Integer>> vertexD = new Vertex<>("D", "456", new ProcessorD());
        Vertex<String> vertexE = new Vertex<>("E", "567", new ProcessorE());
        Vertex<String> vertexF = new Vertex<>("F", "678", new ProcessorF());
        Vertex<Map<String, Object>> vertexG = new Vertex<>("target", "7890", new ProcessorG());
        vertexB.getDependencyNames().add("A");
        vertexC.getDependencyNames().add("A");
        vertexD.getDependencyNames().add("C");
        vertexE.getDependencyNames().add("A");
        vertexE.getDependencyNames().add("C");
        vertexF.getDependencyNames().add("D");
        vertexF.getDependencyNames().add("E");
        vertexG.getDependencyNames().add("F");
        vertexG.getDependencyNames().add("B");
        vertexG.getDependencyNames().add("A");
        return List.of(vertexA, vertexB,
                vertexC, vertexD, vertexE, vertexF, vertexG);
    }
}
