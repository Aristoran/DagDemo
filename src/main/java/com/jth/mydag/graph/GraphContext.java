package com.jth.mydag.graph;

import com.jth.mydag.graph.utils.GraphConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: 图的上下文信息.
 * @author jiatihui
 */
@Component
@Getter @Setter
public class GraphContext<T> {

    /**
     * 图节点映射.
     */
    private Map<String, Vertex<?>> vertexMap;
    /**
     * 图对象结果.
     */
    private T result;

    public GraphContext() {
        this.vertexMap = new HashMap<>(256);
    }

    public GraphContext(List<Vertex<?>> vertices) {
        this();
    }

    public void collectResult() {
        Vertex<T> vertex = (Vertex<T>) vertexMap.get(GraphConstants.TARGET);
        setResult(vertex.getResult());
    }

    /**
     * 重置成员变量
     */
    protected void reset() {
        this.result = null;
    }
}
