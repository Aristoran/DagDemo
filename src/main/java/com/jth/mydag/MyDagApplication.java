package com.jth.mydag;

import com.jth.mydag.graph.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author jiatihui
 */
@SpringBootApplication
@ImportResource(locations = {"classpath:applicationContext.xml"})
public class MyDagApplication {
    @Autowired
    private Graph graph;
    public static void main(String[] args) {
        SpringApplication.run(MyDagApplication.class, args);
    }



}
