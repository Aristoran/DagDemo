package com.jth.mydag.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiatihui
 * @Description: 用于实现子图.
 */
@Getter
public class SubGraphVertex<T> extends Vertex<T> {

    @Setter
    private Map<String, Object> inputs = new ConcurrentHashMap<>();

    /**
     * 节点内部的子图.
     */
    @Setter
    private Graph<T> subGraph;

    public SubGraphVertex() {
    }

    public SubGraphVertex(String name, String id, Graph<T> graph) {
        this(name, id);
        this.subGraph = graph;
    }

    public SubGraphVertex(String name, String id) {
        super(name, id);
    }

    @Override
    public <F> boolean put(F data, String key) {
        if (inputs == null) {
            return false;
        }
        inputs.put(key, data);
        return true;
    }

    /**
     * 直接返回子图的结果.
     */
    @Override
    public CompletableFuture<T> process(Vertex<T> vertex) {
        GraphContext<T> context = subGraph.getContext();
        //确保每个节点都可以拿到入口数据
        subGraph.getVertices().forEach(subVertex -> subVertex.getDependencyData().putAll(inputs));
        subGraph.run();
        return CompletableFuture.completedFuture(context.getResult());
    }

    @Override
    public void reset() {
        super.reset();
        subGraph.reset();
    }
}
