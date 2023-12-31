package com.jth.mydag.controller;

import com.jth.mydag.graph.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author jiatihui
 */
@RestController
@SpringBootApplication
public class GraphController {
    @Autowired
    @Qualifier("graphA")
    private Graph<Map<String, Object>> graph;

    @RequestMapping("/run")
    public Map<String, Object> runGraph() {
        // 在这里调用 graph 的方法
        graph.run();
        Map<String, Object> result = graph.getContext().getResult();
        //重置图
        graph.reset();
        return result;
    }
}
