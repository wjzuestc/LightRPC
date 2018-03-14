package com.rpc.sample.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description: 服务启动引导类
 * @Author: Jingzeng Wang
 * @Date: Created in 21:31  2017/9/2.
 */
public class RpcBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootstrap.class);

    public static void main(String[] args) {
        LOGGER.info("正在启动服务！");
        /**
         * 服务端发布服务，将服务发布到zk上，并启动服务器
         * 会创建所有的bean，而beanfactory是采用的懒加载（所以会在bean生命周期中调用实现了ApplicationContextAware, InitializingBean的方法)
         */
        new ClassPathXmlApplicationContext("spring-service.xml");
    }
}
