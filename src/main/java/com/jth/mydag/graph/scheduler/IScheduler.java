package com.jth.mydag.graph.scheduler;

import com.jth.mydag.graph.Graph;
import com.jth.mydag.graph.Vertex;
import java.util.List;

/**
 * @author jiatihui
 */
public interface IScheduler<T> {

    void schedule(T t);
}
