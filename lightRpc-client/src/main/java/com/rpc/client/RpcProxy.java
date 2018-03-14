package com.rpc.client;

import com.rpc.common.bean.RpcRequest;
import com.rpc.common.bean.RpcResponse;
import com.rpc.register.zookeeper.ZookeeperDiscoverService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @Description: 客户端的rpc代理 创建服务代理
 * @Author: Jingzeng Wang
 * @Date: Created in 18:22  2017/9/20.
 */
public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    /**
     * 通过zk自动发现的服务地址
     */
    private String serviceAddress;

    private ZookeeperDiscoverService zookeeperDiscoverService;

    public RpcProxy(ZookeeperDiscoverService zookeeperDiscoverService) {
        this.zookeeperDiscoverService = zookeeperDiscoverService;
    }

    /**
     * 创建代理对象
     *
     * @param interfaceClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> interfaceClass) {
        return create(interfaceClass, "");
    }

    /**
     * 创建服务代理对象  带有version信息
     * 完成：zk查询服务地址，完成服务调用逻辑
     * TODO {此版本每次调用服务都得去zk查询，可改进成一次查询存储在本地，然后在zk上注册watcher，当服务变动时通知再次查询}
     *
     * @param interfaceClass 目标接口
     * @param serviceVersion 版本号
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> interfaceClass, final String serviceVersion) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建rpc请求，封装为RpcRequest
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setServiceVersion(serviceVersion);
                        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setParameters(args);
                        // 从zookeeper获取服务地址
                        if (zookeeperDiscoverService != null) {
                            // 服务查找地址 接口名_版本号
                            String servicePath = interfaceClass.getName();
                            if (StringUtils.isNotEmpty(serviceVersion)) {
                                servicePath += "-" + serviceVersion;
                            }
                            // 返回一个可用的地址（随机选取）
                            serviceAddress = zookeeperDiscoverService.discover(servicePath);
                            LOGGER.info("客户端获取服务的地址为:" + serviceAddress);
                        } else {
                            LOGGER.error("Zookeeper服务器类注入失败！");
                        }
                        String[] split = StringUtils.split(serviceAddress, ":");
                        String host = split[0];
                        int port = Integer.parseInt(split[1]);
                        // 创建RpcClient进行服务的调用
                        RpcClient rpcClient = new RpcClient(host, port);
                        // 调用服务  获得rpc响应
                        RpcResponse rpcResponse = rpcClient.send(rpcRequest);

                        if (rpcResponse == null) {
                            throw new RuntimeException("响应为null!");
                        }
                        // 获得rpc调用结果
                        if (rpcResponse.getException() != null) {
                            throw rpcResponse.getException();
                        } else {
                            return rpcResponse.getResult();
                        }
                    }
                }
        );
    }
}
