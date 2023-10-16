package com.jth.mydag.graph;

import com.jth.mydag.processor.AbstractProcessor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiatihui
 */
@Getter
@Component
public class Vertex<T> {
    /**
     *  设置唯一名称.
     */
    @Setter
    private String name;
    /**
     * 设置唯一id.
     */
    @Setter
    private String id;

    /**
     * 依赖数据的数量，只有当所有依赖节点都执行完毕，才可执行.
     */
    @Setter
    private AtomicInteger dependencyCount = new AtomicInteger(0);

    /**
     * 依赖的节点
     */
    @Setter
    private Set<Vertex<?>> dependencies;

    /**
     * 存放的依赖节点产出的数据.
     */
    @Setter
    private Map<String, Object> dependencyData = new ConcurrentHashMap<>(100);

    /**
     * 节点处理后的数据存放在此.
     */
    @Setter
    private T result;

    /**
     * 是否激活标识.
     */
    @Setter
    private AtomicBoolean isActivated = new AtomicBoolean(false);

    /**
     * 是否已经执行标识.
     */
    @Setter
    private AtomicBoolean isExecuted = new AtomicBoolean(false);

    /**
     * 执行具体逻辑的processor.
     */
    @Setter
    private AbstractProcessor<T> processor;

    public Vertex() {

    }
    public Vertex(String name, String id) {
        this.name = name;
        this.id = id;
        this.dependencies = new HashSet<>();
    }

    public Vertex(String name, String id,
                  AbstractProcessor<T> processor) {
        this.name = name;
        this.id = id;
        this.dependencies = new HashSet<>();
        this.processor = processor;
    }

    public void addDependency(Vertex<?> vertex) {
        this.dependencies.add(vertex);
    }

    public CompletableFuture<T> process(Vertex<T> vertex) {
        return processor.process(vertex);
    };

    public boolean put(Object data, String key) {
        if (dependencyData == null) {
            return false;
        }
        dependencyData.put(key, data);
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void reset() {
        this.processor.reset();
        this.dependencyCount = new AtomicInteger(0);
        this.dependencyData = new ConcurrentHashMap<>(100);
        this.isExecuted = new AtomicBoolean(false);
        this.isActivated = new AtomicBoolean(false);
        this.result = null;
    }
}
