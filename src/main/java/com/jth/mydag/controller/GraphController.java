package com.jth.mydag.controller;

import com.jth.mydag.graph.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
        return graph.getResult();
    }
}
