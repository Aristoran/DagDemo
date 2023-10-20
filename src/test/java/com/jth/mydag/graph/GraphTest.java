package com.jth.mydag.graph;


import com.jth.mydag.graph.config.AppConfig;
import com.jth.mydag.graph.scheduler.AbstractSubScheduler;
import com.jth.mydag.graph.scheduler.ParallelScheduler;
import com.jth.mydag.processor.processorImpl.ProcessorA;
import com.jth.mydag.processor.processorImpl.ProcessorB;
import com.jth.mydag.processor.processorImpl.ProcessorC;
import com.jth.mydag.processor.processorImpl.ProcessorD;
import com.jth.mydag.processor.processorImpl.ProcessorE;
import com.jth.mydag.processor.processorImpl.ProcessorF;
import com.jth.mydag.processor.processorImpl.ProcessorG;
import com.jth.mydag.processor.processorImpl.ProcessorH;
import com.jth.mydag.processor.processorImpl.ProcessorI;
import com.jth.mydag.processor.processorImpl.ProcessorJ;
import com.jth.mydag.processor.processorImpl.ProcessorK;
import com.jth.mydag.processor.processorImpl.ProcessorL;
import com.jth.mydag.processor.processorImpl.ProcessorM;
import com.jth.mydag.processor.processorImpl.ProcessorN;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class GraphTest {

    Graph<?> graph;

    Graph<String> subGraph;
    AbstractSubScheduler scheduler;
    @Before
    public void init() {
        scheduler = new ParallelScheduler(new AppConfig().executorService(),
                ParallelScheduler.RunStrategy.POOL);
        subGraph = new Graph<>(initVertices2(), scheduler);
        List<Vertex<?>> vertices = initVertices();
        graph = new Graph<>(vertices, scheduler);

    }

    @Test
    public void test() {
        graph.run();
        System.out.println(graph.getContext().getResult());
    }

    @After
    public void shutdown() {
        ((ParallelScheduler) scheduler).executorService.shutdown();
    }

    private List<Vertex<?>> initVertices() {
        Vertex<String> vertexA = new Vertex<>("A", "123", new ProcessorA());
        Vertex<String> vertexB = new Vertex<>("B", "234", new ProcessorB());
        Vertex<Integer> vertexC = new Vertex<>("C", "345", new ProcessorC());
        Vertex<List<Integer>> vertexD = new Vertex<>("D", "456", new ProcessorD());
        Vertex<String> vertexE = new Vertex<>("E", "567", new ProcessorE());
        Vertex<String> vertexF = new Vertex<>("F", "678", new ProcessorF());
        Vertex<String> vertexFf = new SubGraphVertex<String>("subName", "789", subGraph);
        Vertex<Map<String, Object>> vertexG = new Vertex<>("target", "7890", new ProcessorG());
        vertexB.getDependencyNames().add("A");
        vertexC.getDependencyNames().add("A");
        vertexD.getDependencyNames().add("C");
        vertexE.getDependencyNames().add("A");
        vertexE.getDependencyNames().add("C");
        vertexF.getDependencyNames().add("D");
        vertexF.getDependencyNames().add("E");
        vertexFf.getDependencyNames().add("A");
        vertexG.getDependencyNames().add("subName");
        vertexG.getDependencyNames().add("F");
        vertexG.getDependencyNames().add("B");
        vertexG.getDependencyNames().add("A");
        return List.of(vertexA, vertexB,
                vertexC, vertexD, vertexE, vertexF, vertexFf, vertexG);
    }

    private List<Vertex<?>> initVertices2() {
        Vertex<String> vertexH = new Vertex<>("H", "123", new ProcessorH());
        Vertex<String> vertexI = new Vertex<>("I", "234", new ProcessorI());
        Vertex<Integer> vertexJ = new Vertex<>("J", "345", new ProcessorJ());
        Vertex<List<Integer>> vertexK = new Vertex<>("K", "456", new ProcessorK());
        Vertex<String> vertexL = new Vertex<>("L", "567", new ProcessorL());
        Vertex<String> vertexM = new Vertex<>("M", "678", new ProcessorM());
        Vertex<Map<String, Object>> vertexN = new Vertex<>("target", "7890", new ProcessorN());
        vertexI.getDependencyNames().add("H");
        vertexJ.getDependencyNames().add("H");
        vertexK.getDependencyNames().add("J");
        vertexL.getDependencyNames().add("H");
        vertexL.getDependencyNames().add("J");
        vertexM.getDependencyNames().add("K");
        vertexM.getDependencyNames().add("L");
        vertexN.getDependencyNames().add("M");
        vertexN.getDependencyNames().add("I");
        vertexN.getDependencyNames().add("H");
        return List.of(vertexH, vertexI,
                vertexJ, vertexK, vertexL, vertexM, vertexN);
    }
}
