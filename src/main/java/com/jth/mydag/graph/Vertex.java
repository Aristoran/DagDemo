package com.jth.mydag.graph;

import com.jth.mydag.processor.AbstractProcessor;
import com.jth.mydag.processor.IProcessor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
     *  设置唯一名称作为产出依据.
     */
    @Setter
    protected String name;
    /**
     * 设置唯一id.
     */
    @Setter
    protected String id;

    /**
     * 依赖的数据源name.
     */
    @Setter
    protected List<String> dependencyNames = new ArrayList<>();

    /**
     * 依赖数据的数量，只有当所有依赖节点都执行完毕，才可执行.
     */
    @Setter
    protected AtomicInteger dependencyCount = new AtomicInteger(0);

    /**
     * 依赖的节点
     */
    @Setter
    protected Set<Vertex<?>> dependencies;

    /**
     * 存放的依赖节点产出的数据.
     */
    @Setter
    protected Map<String, Object> dependencyData = new ConcurrentHashMap<>(100);

    /**
     * 节点处理后的数据存放在此.
     */
    @Setter
    protected T result;

    /**
     * 是否激活标识.
     */
    @Setter
    protected AtomicBoolean isActivated = new AtomicBoolean(false);

    /**
     * 是否已经执行标识.
     */
    @Setter
    protected AtomicBoolean isExecuted = new AtomicBoolean(false);

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

    public <E> boolean put(E data, String key) {
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
        if (this.processor != null) {
            this.processor.reset();
        }
        this.dependencyCount = new AtomicInteger(0);
        this.dependencyData = new ConcurrentHashMap<>(100);
        this.isExecuted = new AtomicBoolean(false);
        this.isActivated = new AtomicBoolean(false);
        this.result = null;
    }
}
