package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import org.springframework.stereotype.Component;

/**
 * @author jiatihui
 * @Description: 串行调度任务器.
 */
@Component
public class SerialScheduler extends AbstractSubScheduler {

    public SerialScheduler() {
        super();
    }

    /**
     * 图执行入口.
     * @param graph 要执行的图.
     */
    @Override
    public void executeTask(Graph<?> graph) {
        executeSerial(graph);
    }

    /**
     * 串行执行图方式.
     */
    public void executeSerial(Graph<?> graph) {
        Vertex<?> vertex;
        while ((vertex = graph.getExecutionQueue().poll()) != null) {
            graph.processVertex(vertex);
        }
    }
}
