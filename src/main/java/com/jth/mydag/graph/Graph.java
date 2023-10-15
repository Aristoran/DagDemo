package com.jth.mydag.graph;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jiatihui
 * @Description: 图对象.
 */

@Log4j2
@Component
public class Graph<T> {
    private static final String TARGET = "target";
    /**
     * 执行队列.
     */
    private final Queue<Vertex<?>> executionQueue = new ConcurrentLinkedQueue<>();
    /**
     * 激活队列.
     */
    private final Queue<Vertex<?>> activationQueue = new ConcurrentLinkedQueue<>();
    @Getter@Setter
    private List<Vertex<?>> vertices;
    @Getter@Setter
    private ConcurrentHashMap<String, Vertex<?>> vertexMap;

    private final Scheduler<T> scheduler;

    @Getter@Setter
    private T result;

    private RunStrategy strategy = RunStrategy.PARALLEL;


    public Graph() {
        this.vertices = new ArrayList<>(200);
        this.vertexMap = new ConcurrentHashMap<>(256);
        this.scheduler = new Scheduler<>(activationQueue, executionQueue);
    }

    public Graph(List<Vertex<?>> vertices) {
        this.vertices = vertices;
        initVertexMap(); //初始化map
        activateVertex(); //激活图
        this.scheduler = new Scheduler<>(activationQueue, executionQueue);
    }

    public Graph(List<Vertex<?>> vertices, RunStrategy strategy) {
        this.vertices = vertices;
        initVertexMap(); //初始化map
        activateVertex(); //激活图
        this.scheduler = new Scheduler<>(activationQueue, executionQueue);
        this.strategy = strategy;
    }

    public <E> void addVertex(Vertex<E> vertex) {
        vertexMap.put(vertex.getId(), vertex);
    }

    private void initVertexMap() {
        vertexMap = new ConcurrentHashMap<>();
        vertices.forEach(vertex -> vertexMap.put(vertex.getName(), vertex));
    }

    public void parseGraph() {
        initVertexMap();
    }

    /**
     * 从map中获取目标节点，并激活它。
     */
    public void activateVertex() {
        Vertex vertex = vertexMap.get(TARGET);
        activate(vertex);
    }

    /**
     * 激活全图节点的方法。
     * 1、首先检查图中是否存在环，如果存在则抛出异常.
     * 2、如果节点已经被激活过，则直接返回, 否则，将节点标记为已激活，并将其添加到激活队列中.
     * 3、遍历激活队列，对每个节点的依赖项进行激活，并增加依赖计数.
     * 4、如果顶点的依赖计数为0，说明该顶点没有依赖项，可以直接添加到执行队列中.
     *
     * @param vertex 要激活的顶点.
     * @throws IllegalArgumentException 如果图中存在环，抛出该异常.
     */
    public void activate(Vertex<?> vertex) {
        if (isCycle(vertex)) {
            throw new IllegalArgumentException("Cycle detected in graph");
        }
        if (vertex.getIsActivated().get()) {
            return;
        }
        vertex.setIsActivated(new AtomicBoolean(true));
        activationQueue.add(vertex);
        while (!activationQueue.isEmpty()) {
            Vertex<?> currentVertex = activationQueue.poll();
            for (Vertex<?> dependency : currentVertex.getDependencies()) {
                currentVertex.getDependencyCount().incrementAndGet();
                activate(dependency);
            }
            if (currentVertex.getDependencyCount().get() == 0) {
                executionQueue.add(currentVertex);
            }
        }
    }

    /**
     * 判断图是否有环.
     * @param vertex 从目标节点开始判断.
     * @return boolean.
     */
    private boolean isCycle(Vertex<?> vertex) {
        Set<Vertex<?>> visited = new HashSet<>();
        Set<Vertex<?>> recursionStack = new HashSet<>();
        return checkCycle(vertex, visited, recursionStack);
    }

    /**
     * 具体判断环的逻辑.
     * 从给定节点开始依次递归，使用recursionStack检查有无环存在.
     * 递归结束后，若无,则将该节点从递归栈中删除并存放在已检查节点列表中.
     * 如果递归栈中没有，但是已检查列表中有，证明该节点已检查过且无环，不再重复检查.
     * @param vertex 入口节点.
     * @param visited 已经检查过的节点.
     * @param recursionStack 递归栈，用于检测环.
     */
    private boolean checkCycle(Vertex<?> vertex,
                               Set<Vertex<?>> visited,
                               Set<Vertex<?>> recursionStack) {
        if (recursionStack.contains(vertex)) {
            return true;
        }
        if (visited.contains(vertex)) {
            return false;
        }
        visited.add(vertex);
        recursionStack.add(vertex);
        for (Vertex<?> dependency : vertex.getDependencies()) {
            if (checkCycle(dependency, visited, recursionStack)) {
                return true;
            }
        }
        recursionStack.remove(vertex);
        return false;
    }

    /**
     * 处理节的方法.
     * 1、执行当前节点并设置结果和执行状态.
     * 2、遍历所有的节点：如果依赖计数为0，那么就会将这个顶点添加到执行队列中.
     * @param vertex 当前需要处理的节点.
     */
    public  <E> void processVertex(Vertex<E> vertex) {
        // 处理当前顶点并设置结果
        processFutureTask(vertex);
        // 设置当前顶点为已执行状态
        vertex.setIsExecuted(new AtomicBoolean(true));
        // 遍历所有的节点
        for (Vertex dependentVertex : vertexMap.values()) {
            // 如果节点已经执行过，那么就跳过
            if (vertex == dependentVertex || dependentVertex.getIsExecuted().get()) {
                continue;
            }
            // 如果节点依赖于当前节点
            if (dependentVertex.getDependencies().contains(vertex)) {
                // 将执行结果传递给依赖于当前节点的节点
                if (!dependentVertex.put(vertex.getResult(), vertex.getName())) {
                    log.error("vertex put dependency result error!");
                    break;
                };
                // 执行成功，减少依赖计数
                dependentVertex.getDependencyCount().decrementAndGet();
                // 如果依赖计数为0，那么就将这个顶点添加到执行队列中, 如果有两个节点并发执行，
                // 其中一个状态还是false, 仍旧会被添加到队列中来，
                // 所以包在判断语句内部，只有依赖当前节点的节点可以执行
                if (dependentVertex.getDependencyCount().get() == 0) {
                    executionQueue.add(dependentVertex);
                }
            }
        }
    }

    private static <E> void processFutureTask(Vertex<E> vertex) {
        CompletableFuture<E> future = vertex.process(vertex);
        AtomicReference<E> result = new AtomicReference<>();
        future.thenApply(r -> {
            result.set(r);
            return null;
        }).exceptionally(e -> {
            log.error("process result error!");
            return null;
        });
        vertex.setResult(result.get());
    }

    /**
     * 真正执行图调度入口
     */
    public void run() {
        initVertexMap();
        activateVertex(); //执行前需要激活
        switch (strategy) {
            case SERIAL:
                scheduler.executeSerial(this);
                break;
            case PARALLEL_WITH_FUTURE:
                scheduler.executeByFuture(this);
                break;
            case PARALLEL:
            default:
                scheduler.executeByPool(this);
        }
        Vertex<T> vertex = (Vertex<T>) vertexMap.get(TARGET);
        setResult(vertex.getResult());
    }

    public enum RunStrategy {
        SERIAL,
        PARALLEL,
        PARALLEL_WITH_FUTURE

    }

}
