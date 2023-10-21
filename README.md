# 简易DAG调度实现DEMO

clone代码后，直接运行springBoot项目，请求localhost:8080/run

## 定义图
- 具体可以参考applicationContext.xml配置
```xml
    <bean id = "graphA" class="com.jth.mydag.graph.Graph">
        <property name="context" ref="graphContext"/>
        <property name="scheduler" ref = "schedulerParallel"/>
        <property name="vertices">
            <list>
                <ref bean="vertexA"/>
                <ref bean="vertexB"/>
                <ref bean="vertexC"/>
                <ref bean="vertexD"/>
                <ref bean="vertexE"/>
                <ref bean="vertexF"/>
                <ref bean="subGraphVertex"/>
                <ref bean="vertexG"/>
            </list>
        </property>
    </bean>

    <bean id = "graphContext" class="com.jth.mydag.graph.GraphContext">
    </bean>
```

## 定义节点
- dependencyNames 为要依赖的数据
```xml
    <bean id = "vertexF" class="com.jth.mydag.graph.Vertex">
        <constructor-arg name="name" value="F"/>
        <constructor-arg name="id" value="5"/>
        <property name="processor" ref="processorF"/>
        <property name="dependencyNames">
            <list>
                <value>E</value>
                <value>D</value>
            </list>
        </property>
    </bean>

    <bean id = "vertexG" class="com.jth.mydag.graph.Vertex">
        <constructor-arg name="name" value="target"/>
        <constructor-arg name="id" value="6"/>
        <property name="processor" ref="processorG"/>
        <property name="dependencyNames">
            <list>
                <value>A</value>
                <value>B</value>
                <value>F</value>
            </list>
        </property>
    </bean>
```

## 定义子图

- 提供子图的调度功能，子图对外一个入口，一个出口。
- 子图以子节点的形式定义，调用到当前节点，会执行子图的调度逻辑
```xml
    <bean id = "subGraph" class="com.jth.mydag.graph.Graph">
        <property name="context" ref="subGraphContext"/>
        <property name="scheduler" ref = "schedulerParallel"/>
        <property name="vertices">
            <list>
                <ref bean="vertexH"/>
                <ref bean="vertexI"/>
                <ref bean="vertexJ"/>
                <ref bean="vertexK"/>
                <ref bean="vertexL"/>
                <ref bean="vertexM"/>
                <ref bean="vertexN"/>
            </list>
        </property>
    </bean>

    <bean id = "subGraphVertex" class="com.jth.mydag.graph.SubGraphVertex">
        <constructor-arg name="name" value="subName"/>
        <constructor-arg name="id" value="5"/>
        <property name="subGraph" ref="subGraph"/>
        <property name="dependencyNames">
            <list>
                <value>A</value>
            </list>
        </property>
    </bean>

```
