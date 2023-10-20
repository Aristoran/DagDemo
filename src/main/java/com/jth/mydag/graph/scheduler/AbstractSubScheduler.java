package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author jiatihui
 * @Description: 图节点任务调度器抽象类.
 */
@Getter
@Log4j2
public abstract class AbstractSubScheduler implements IScheduler<Graph<?>> {


    public AbstractSubScheduler() {
    }

    /**
     * 子类实现具体策略
     * @param graph 要执行的图.
     */
    @Override
    public void schedule(Graph<?> graph) {
        executeTask(graph);
        graph.getContext().collectResult();//结果收集
    }

    public abstract void executeTask(Graph<?> graph);
}
