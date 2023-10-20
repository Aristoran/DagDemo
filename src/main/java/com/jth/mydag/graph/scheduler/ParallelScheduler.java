package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import com.jth.mydag.graph.utils.GraphConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author jiatihui
 * @Description: 图节点任务调度器，提供两种并发调度.
 */
@Log4j2
@Component
@Getter @Setter
public class ParallelScheduler extends AbstractSubScheduler {

    /**
     * 自定义线程池.
     */
    @Autowired
    public ExecutorService executorService;

    private RunStrategy strategy = RunStrategy.POOL;

    public ParallelScheduler() {
        super();
    }

    public ParallelScheduler(ExecutorService executorService) {
        this.executorService =  executorService;
    }

    public ParallelScheduler(ExecutorService executorService, RunStrategy strategy) {
        this(executorService);
        this.strategy = strategy;
    }

    @Override
    public void executeTask(Graph<?> graph) {
        executeByStrategy(graph);
    }

    /**
     * 根据策略执行图调度，线程池 or CompletableFuture.
     */
    public void executeByStrategy(Graph<?> graph) {
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
    public void executeByPool(Graph<?> graph) {
        Vertex<?> vertex;
        //图执行的最终节点
        Vertex<?> targetVertex = graph.getContext().getVertexMap()
                .get(GraphConstants.TARGET);
        while (!targetVertex.getIsExecuted().get()) {
            if ((vertex = graph.getExecutionQueue().poll()) != null) {
                executorService.submit(new Task<>(vertex, graph));
            }
        }
    }

    /**
     * 使用线程池实现调度逻辑.
     */
    public void execute(Graph<?> graph) {
        Vertex<?> vertex;
        while (graph.getExecutedCount().get()
                != graph.getActivatedCount().get()) {
            if ((vertex = graph.getExecutionQueue().poll()) != null) {
                executorService.submit(new Task<>(vertex, graph));
            }
        }
    }

    /**
     * 执行图调度入口.
     */
    public void executeGraph(Graph<?> graph) {
        execute(graph);
    }

    /**
     * 执行调度入口，使用CompletableFuture实现.
     */
    public void executeByFuture(Graph<?> graph) {
        Vertex<?> vertex;
        //图执行的最终节点
        Vertex<?> targetVertex = graph.getContext().getVertexMap()
                .get(GraphConstants.TARGET);
        while (!targetVertex.getIsExecuted().get()) {
            if ((vertex = graph.getExecutionQueue().poll()) != null) {
                processVertexAsync(new Task<>(vertex, graph));
            }
        }
    }

    /**
     * 异步处理任务的方法。异步处理当前顶点并设置结果和执行状态.
     */
    private void processVertexAsync(Task<?> task) {
        CompletableFuture.runAsync(task);
    }

    /**
     * 容器销毁时提前释放线程池.
     */
    @PreDestroy
    private void cleanup() {
        executorService.shutdown();
    }
    public static class Task<E> implements Runnable {
        private final Vertex<E> vertex;
        private final Graph<?> graph;
        public Task(Vertex<E> vertex, Graph<?> graph) {
            this.vertex = vertex;
            this.graph = graph;
        }

        @Override
        public void run() {
            try {
                graph.processVertex(vertex);
            } catch (RuntimeException re) {
                log.error("graph process error: " + re.getMessage());
            }
        }
    }

    public enum RunStrategy {
        POOL,
        FUTURE

    }
}
