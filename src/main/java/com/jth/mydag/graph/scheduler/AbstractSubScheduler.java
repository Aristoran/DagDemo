package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;

/**
 * @author jiatihui
 * @Description: 图节点任务调度器抽象类.
 */
@Getter
@Log4j2
@Scope("prototype")
public abstract class AbstractSubScheduler<T> implements IScheduler<Graph<T>> {

    /**
     * 图激活队列.
     */
    @Setter
    public Queue<Vertex<?>> activationQueue;
    /**
     * 图执行队列.
     */
    @Setter
    public Queue<Vertex<?>> executionQueue;

    public AbstractSubScheduler() {
        this.activationQueue = new ConcurrentLinkedQueue<>();
        this.executionQueue = new ConcurrentLinkedQueue<>();
    }

    public AbstractSubScheduler(Queue<Vertex<?>> activationQueue,
                     Queue<Vertex<?>> executionQueue) {
        this.activationQueue = activationQueue;
        this.executionQueue = executionQueue;
    }



    /**
     * 子类实现具体策略
     * @param graph 要执行的图.
     */
    @Override
    public void schedule(Graph<T> graph) {
        executeTask(graph);
        graph.getContext().collectResult();//结果收集
    }

    public abstract void executeTask(Graph<T> graph);

    public void reset() {
        activationQueue.clear();
        executionQueue.clear();
    };
}
