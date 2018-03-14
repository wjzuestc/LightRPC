package com.rpc.sample.hello;

/**
 * @Description: 服务接口
 * @Author: Jingzeng Wang
 * @Date: Created in 20:31  2017/8/20.
 */
public interface HelloWorldService {
    /**
     * 远程调用方法
     * @param name
     * @return
     */
    String helloWorld(String name);
}
