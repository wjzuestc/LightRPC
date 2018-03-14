# 轻量级分布式LightRPC简介

实现了轻量级 RPC 框架，框架基于 TCP 协议，提供了 NIO 特性，提供高效的序列化方式，同时也具备服务注册与发现的能力。将框架服务端部署到分布式环境中的任意节点，可实现服务端与
客户端的开发完全分离。基于Netty、Zookeeper、Protostuff。

## 项目模块介绍

  **light-rpc 模块介绍**
  
    - lightRpc-server  RPC服务端：完成服务注册，接收客户端连接，完成服务调用
    - lightRpc-client  PRC客户端：完成服务发现，连接服务端进行服务调用
    - lightRpc-register  zk注册：完成服务的注册和发现
    - lightRpc-common  common支持：请求、响应的封装，编解码协议；序列化
    - lightRpc-demo   测试用例：远程调用服务，完成本RPC的应用测试

## lightRpc的部署使用

本PPC框架可以无缝结合Spring框架。Spring 提供依赖注入与参数配置，Netty实现高性能网络传输，ZooKeeper实现服务注册与发现。

**在加入了本项目工程后，其在Spring配置文件中配置的方法为：**

 **客户端** 
```java
    <context:property-placeholder location="client.properties"/>

    <!--完成zookeeper的订阅发现服务-->
    <bean id="zkServiceDiscover" class="com.rpc.register.zookeeper.ZookeeperDiscoverService">
        <constructor-arg name="zkRegisterAddress" value="${zk.register.address}"/>
    </bean>
    <!--配置rpc代理，去订阅和调用服务-->
    <bean id="rpcProxy" class="com.rpc.client.RpcProxy">
        <constructor-arg name="zookeeperDiscoverService" ref="zkServiceDiscover"/>
    </bean>
    
```
 **服务端** 
 ```java

    <context:property-placeholder location="service.properties"/>

    <!--完成服务注册到zookeeper-->
    <bean id="zkRegisterService" class="com.rpc.register.zookeeper.ZookeeperRegisterService">
        <constructor-arg name="zkRegisterAddress" value="${zk.register.address}"/>
    </bean>
    <!--配置rpc服务,完成服务注册及调用处理-->
    <bean id="rpcServer" class="com.rpc.server.RpcServer">
        <constructor-arg name="rpcServiceAddress" value="${service.address}"/>
        <constructor-arg name="zookeeperRegisterService" ref="zkRegisterService"/>
    </bean>
```

配置完成后，服务端加载对应的Spring配置文件，即可完成服务端发布与服务器的启动。

**客户端调用服务** 

```java
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        // 获得rpc代理类
        HelloWorldService helloService = rpcProxy.create(HelloWorldService.class);
        // 通过代理-远程调用服务获得执行结果
        String result = helloService.helloWorld("我是rpc！");
        System.out.println(result);
        //带有版本号的调用  服务要对应此version
        HelloWorldService helloService2 = rpcProxy.create(HelloWorldService.class, "sample.hello2");
        String result2 = helloService2.helloWorld("hello word");
        System.out.println(result2);
```

按照上面介绍的方式即可完成远程服务调用，且服务升级可利用升级版本号进行升级。

