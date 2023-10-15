package com.jth.mydag.graph;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 图的上下文信息.
 * @author jiatihui
 */
@Component
@Getter @Setter
public class GraphContext<T> {
    /**
     * 图节点.
     */
    private List<Vertex<?>> vertices;
    /**
     * 图节点映射.
     */
    private ConcurrentHashMap<String, Vertex<?>> vertexMap;
    /**
     * 图对象结果.
     */
    private T result;

    public GraphContext() {
        this.vertices = new ArrayList<>(200);
        this.vertexMap = new ConcurrentHashMap<>(256);
    }

    public GraphContext(List<Vertex<?>> vertices) {
        this();
        this.vertices = vertices;
    }
}