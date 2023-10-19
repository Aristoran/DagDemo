package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import com.jth.mydag.graph.config.AppConfig;
import com.jth.mydag.graph.utils.FutureUtils;
import com.jth.mydag.graph.utils.GraphConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author jiatihui
 * @Description: 图节点任务调度器，提供两种并发调度.
 */
@Log4j2
@Component
@Getter @Setter
public class ParallelScheduler<T> extends AbstractSubScheduler<T> {

    /**
     * 自定义线程池.
     */
    @Autowired
    public ExecutorService executorService;

    private RunStrategy strategy = RunStrategy.POOL;

    public ParallelScheduler() {
        super();
    }
    public ParallelScheduler(ExecutorService executorService,
                     Queue<Vertex<?>> activationQueue,
                     Queue<Vertex<?>> executionQueue) {
        super(activationQueue, executionQueue);
        this.executorService =  executorService;
    }

    public ParallelScheduler(ExecutorService executorService,
                             Queue<Vertex<?>> activationQueue,
                             Queue<Vertex<?>> executionQueue, RunStrategy strategy) {
        this(executorService, activationQueue, executionQueue);
        this.strategy = strategy;
    }

    @Override
    public void executeTask(Graph<T> graph) {
        executeByStrategy(graph);
    }

    /**
     * 根据策略执行图调度，线程池 or CompletableFuture.
     */
    public void executeByStrategy(Graph<T> graph) {
        switch (strategy) {
            case FUTURE:
                executeByFuture(graph);
                break;
            case POOL:
            default:
                executeGraph(graph);
                break;
        }
    }

    /**
     * 使用线程池实现调度逻辑.
     */
    public void executeByPool(Graph<T> graph) {
        Vertex<?> vertex;
        //图执行的最终节点
        Vertex<?> targetVertex = graph.getContext().getVertexMap()
                .get(GraphConstants.TARGET);
        while (!targetVertex.getIsExecuted().get()) {
            if ((vertex = executionQueue.poll()) != null) {
                executorService.submit(new Task<>(vertex, graph));
            }
        }
    }

    /**
     * 使用线程池实现调度逻辑.
     */
    public void execute(Graph<T> graph) {
        Vertex<?> vertex;
        while (graph.getExecutedCount().get()
                != graph.getActivatedCount().get()) {
            if ((vertex = executionQueue.poll()) != null) {
                executorService.submit(new Task<>(vertex, graph));
            }
        }
    }

    /**
     * 执行图调度入口.
     */
    public void executeGraph(Graph<T> graph) {
        execute(graph);
    }

    /**
     * 执行调度入口，使用CompletableFuture实现.
     */
    public void executeByFuture(Graph<T> graph) {
        List<CompletableFuture<Void>> futures
                = new ArrayList<>(executionQueue.size());
        Vertex<?> vertex;
        //图执行的最终节点
        Vertex<?> targetVertex = graph.getContext().getVertexMap()
                .get(GraphConstants.TARGET);
        while (!targetVertex.getIsExecuted().get()) {
            if ((vertex = executionQueue.poll()) != null) {
                futures.add(processVertexAsync(new Task<>(vertex, graph)));
            }
        }
    }

    /**
     * 异步处理任务的方法。异步处理当前顶点并设置结果和执行状态.
     ** @return CompletableFuture<Void> 返回一个CompletableFuture，表示异步计算的结果.
     */
    private CompletableFuture<Void> processVertexAsync(Task<?> task) {
        return CompletableFuture.runAsync(task);
    }

    /**
     * 容器销毁时提前释放线程池.
     */
    @PreDestroy
    private void cleanup() {
        executorService.shutdown();
    }
    public class Task<E> implements Runnable {
        private final Vertex<E> vertex;
        private final Graph<T> graph;
        public Task(Vertex<E> vertex, Graph<T> graph) {
            this.vertex = vertex;
            this.graph = graph;
        }

        @Override
        public void run() {
            try {
                graph.processVertex(vertex);
            } catch (RuntimeException re) {
                log.error("Error processing vertex: " + vertex.getId(), re);
            }
        }
    }

    public enum RunStrategy {
        POOL,
        FUTURE

    }
}
