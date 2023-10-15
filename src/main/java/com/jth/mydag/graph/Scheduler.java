package com.jth.mydag.graph;

import com.jth.mydag.graph.utils.FutureUtils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author jiatihui
 * @Description: 图节点任务调度器，提供串行、并行两种调度方式
 */
@Log4j2
public class Scheduler<T> {
    private ExecutorService executorService = new ThreadPoolExecutor(
            64,
            128,
            500L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());;
    private final Queue<Vertex<?>> activationQueue;
    private final Queue<Vertex<?>> executionQueue;
    public Scheduler() {
        this.activationQueue = new ConcurrentLinkedQueue<>();
        this.executionQueue = new ConcurrentLinkedQueue<>();
    }

    public Scheduler(ExecutorService executorService,
                     Queue<Vertex<?>> activationQueue,
                     Queue<Vertex<?>> executionQueue) {
        this.executorService =  executorService;
        this.activationQueue = activationQueue;
        this.executionQueue = executionQueue;
    }

    public Scheduler(Queue<Vertex<?>> activationQueue,
                     Queue<Vertex<?>> executionQueue) {
        this.activationQueue = activationQueue;
        this.executionQueue = executionQueue;
    }

    /**
     * 串行执行图方式.
     */
    public  void executeSerial(Graph<T> graph) {
        Vertex<?> vertex;
        while ((vertex = executionQueue.poll()) != null) {
            graph.processVertex(vertex);
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

    private class Task<E> implements Runnable {
        private final Vertex<E> vertex;
        private final Graph<T> graph;
        public Task(Vertex<E> vertex, Graph<T> graph) {
            this.vertex = vertex;
            this.graph = graph;
        }

        @Override
        public void run() {
            graph.processVertex(vertex);
        }
    }
}
