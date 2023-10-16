package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import com.jth.mydag.graph.config.AppConfig;
import com.jth.mydag.graph.utils.FutureUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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

    @Override
    public void executeTask(Graph<T> graph) {
        executeByStrategy(graph);
    }

    @Override
    public void reset() {
        super.reset();
        this.executorService = new AppConfig().executorService();
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
        List<Future<?>> futures = new ArrayList<>();
        while ((vertex = executionQueue.poll()) != null) {
            Task<?> task = new Task<>(vertex, graph);
            Future<?> future = executorService.submit(task);
            futures.add(future);
        }
        for (Future<?> future : futures) {
            try {
                future.get(); // 等待任务完成
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        if (!executionQueue.isEmpty()) {
            executeByPool(graph);
        }
    }

    /**
     * 执行图调度入口.
     */
    public void executeGraph(Graph<T> graph) {
        executeByPool(graph);
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 执行调度入口，使用CompletableFuture实现.
     */
    public void executeByFuture(Graph<T> graph) {
        List<CompletableFuture<Void>> futures
                = new ArrayList<>(executionQueue.size());
        Vertex<?> vertex;
        while ((vertex = executionQueue.poll()) != null) {
            futures.add(processVertexAsync(new Task<>(vertex, graph)));
        }

        try {
            FutureUtils.collect(futures);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 异步处理任务的方法。异步处理当前顶点并设置结果和执行状态.
     ** @return CompletableFuture<Void> 返回一个CompletableFuture，表示异步计算的结果.
     */
    private CompletableFuture<Void> processVertexAsync(Task<?> task) {
        return CompletableFuture.runAsync(task)
                .thenRunAsync(() -> {
                    if (!executionQueue.isEmpty()) {
                        executeByFuture(task.graph);
                    }
                });
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
