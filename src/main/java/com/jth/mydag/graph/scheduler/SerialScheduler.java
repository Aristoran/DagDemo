package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import org.springframework.stereotype.Component;

import java.util.Queue;

/**
 * @author jiatihui
 * @Description: 串行调度任务器.
 */
@Component
public class SerialScheduler<T> extends AbstractSubScheduler<T> {

    public SerialScheduler() {
        super();
    }
    public SerialScheduler(Queue<Vertex<?>> activationQueue,
                           Queue<Vertex<?>> executionQueue) {
        super(activationQueue, executionQueue);
    }

    /**
     * 图执行入口.
     * @param graph 要执行的图.
     */
    @Override
    public void executeTask(Graph<T> graph) {
        executeSerial(graph);
    }

    /**
     * 串行执行图方式.
     */
    public void executeSerial(Graph<T> graph) {
        Vertex<?> vertex;
        while ((vertex = executionQueue.poll()) != null) {
            graph.processVertex(vertex);
        }
    }
}
