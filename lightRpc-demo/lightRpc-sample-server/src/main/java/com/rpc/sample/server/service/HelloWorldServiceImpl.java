package com.rpc.sample.server.service;

import com.rpc.sample.hello.HelloWorldService;
import com.rpc.server.annotation.LightRpcService;

/**
 * @Description: 服务实现
 * @Author: Jingzeng Wang
 * @Date: Created in 20:34  2017/8/20.
 */
//注解指明实现的接口  因为可能实现多个接口
@LightRpcService(value = HelloWorldService.class)
public class HelloWorldServiceImpl implements HelloWorldService {
    /**
     * 远程调用接口实现类
     * @param name
     * @return
     */
    @Override
    public String helloWorld(String name) {
        return "HelloWorld!!!" + name;
    }
}
