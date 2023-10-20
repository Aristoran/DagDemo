package com.jth.mydag.graph;

import com.jth.mydag.graph.scheduler.AbstractSubScheduler;
import com.jth.mydag.graph.scheduler.ParallelScheduler;
import com.jth.mydag.graph.utils.GraphConstants;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jiatihui
 * @Description: 图对象.
 */
@Log4j2
@Component
public class Graph<T> {

    /**
     * 图激活队列.
     */
    @Setter @Getter
    private Queue<Vertex<?>> activationQueue = new ConcurrentLinkedQueue<>();

    /**
     * 图执行队列.
     */
    @Setter @Getter
    private Queue<Vertex<?>> executionQueue = new ConcurrentLinkedQueue<>();

    /**
     * 图节点.
     */
    @Getter @Setter
    private List<Vertex<?>> vertices = new ArrayList<>(200);

    /**
     * 已执行的图节点数量.
     */
    @Getter @Setter
    private AtomicInteger executedCount = new AtomicInteger(0);

    /**
     * 已激活的图节点数量.
     */
    @Getter @Setter
    private AtomicInteger activatedCount = new AtomicInteger(0);

    /**
     * 图上下文，存放图数据结果、图节点信息.
     */
    @Getter @Setter
    private GraphContext<T> context;

    /**
     * 任务执行器
     */
    @Setter
    private AbstractSubScheduler scheduler;

    public Graph() {
        this.context = new GraphContext<>();
        this.scheduler = new ParallelScheduler();
    }

    public Graph(List<Vertex<?>> vertices) {
        this.vertices = vertices;
        this.context = new GraphContext<>(vertices);
        this.scheduler = new ParallelScheduler();
    }

    public Graph(List<Vertex<?>> vertices, AbstractSubScheduler scheduler) {
        this(vertices);
        this.scheduler = scheduler;
    }

    public <E> void addVertex(Vertex<E> vertex) {
        context.getVertexMap().put(vertex.getId(), vertex);
    }

    private void initVertices() {
        vertices.forEach(vertex
                -> context.getVertexMap().put(vertex.getName(), vertex));
        vertices.parallelStream().forEach(vertex
                -> vertex.getDependencyNames().forEach(name
                -> {
                    Vertex<?> dependency = context.getVertexMap().get(name);
                    if (dependency == null) {
                        log.error(String.format("%s dependency %s is null", vertex.getName(), name));
                        return;
                    }
                    vertex.addDependency(dependency);
                }));
        activateVertex(); //图激活
    }

    public void parseGraph() {
        initVertices();
    }

    /**
     * 从map中获取目标节点，并激活它。
     */
    private void activateVertex() {
        Vertex<?> vertex = context.getVertexMap().get(GraphConstants.TARGET);
        activate(vertex);
    }

    /**
     * 激活全图节点的方法。
     * 1、首先检查图中是否存在环，如果存在则抛出异常.
     * 2、如果节点已经被激活过，则直接返回, 否则，将节点标记为已激活，并将其添加到激活队列中.
     * 3、遍历激活队列，对每个节点的依赖项进行激活，并增加依赖计数.
     * 4、如果顶点的依赖计数为0，说明该顶点没有依赖项，可以直接添加到执行队列中.
     *
     * @param vertex 要激活的节点.
     * @throws IllegalArgumentException 如果图中存在环，抛出该异常.
     */
    private void activate(Vertex<?> vertex) {
        if (isCycle(vertex)) {
            throw new IllegalArgumentException("Cycle detected in graph");
        }
        if (vertex.getIsActivated().get()) {
            return;
        }
        vertex.setIsActivated(new AtomicBoolean(true));
        activatedCount.incrementAndGet();
        activationQueue.add(vertex);
        Vertex<?> currentVertex;
        while ((currentVertex = activationQueue.poll()) != null) {
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
            log.error(String.format("vertex %s may be visited, graph is cycle!", vertex.getName()));
            return true;
        }
        if (visited.contains(vertex)) {
            return false;
        }
        visited.add(vertex);
        recursionStack.add(vertex);
        for (Vertex<?> dependency : vertex.getDependencies()) {
            if (dependency == null) {
                log.error("dependency is null, check cycle interrupt!");
                return true;
            }
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
    public <E> void processVertex(Vertex<E> vertex) {
        log.error(String.format("vertex %s executing by %s", vertex.getName(), Thread.currentThread().getName()));
        // 处理当前顶点并设置结果
        processFutureTask(vertex);
        // 设置当前顶点为已执行状态
        vertex.setIsExecuted(new AtomicBoolean(true));
        log.error(String.format("vertex %s executed", vertex.getName()));
        // 执行数量+1
        executedCount.incrementAndGet();
        // 遍历所有的节点
        for (Vertex<?> dependentVertex : vertices) {
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
                // 如果依赖计数为0，那么就将这个节点添加到执行队列中, 如果有两个节点并发执行，
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
     * 真正执行图调度入口.
     */
    public void run() {
        initVertices();//执行前初始化图
        long start = System.currentTimeMillis();
        scheduler.schedule(this);
        long end = System.currentTimeMillis();
        log.error("time cost is: " + (end - start) + "ms");
    }

    /**
     * 图重置.
     */
    public void reset() {
        vertices.forEach(Vertex::reset);
        context.reset();
        activationQueue.clear();
        executionQueue.clear();
    }

}
