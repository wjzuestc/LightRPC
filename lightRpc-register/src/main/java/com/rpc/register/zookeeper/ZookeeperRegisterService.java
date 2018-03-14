package com.rpc.register.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 以服务接口名_版本号的形式注册服务到zk上
 * @Author: Jingzeng Wang
 * @Date: Created in 21:06  2017/8/20.
 */
public class ZookeeperRegisterService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegisterService.class);

    private final ZkClient zkClient;

    public ZookeeperRegisterService(String zkRegisterAddress) {
        //使用zkclient连接zookeeper，简化原始zookeeper操作  在构造函数中确保只会创建一个zkclient
        zkClient = new ZkClient(zkRegisterAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.info("连接zookeeper!");
    }

    /**
     * 将服务注册到zk上，以服务名为路径
     * 路径为：/register/serviceName/address-节点顺序{value}
     * 最后一个为顺序临时节点 在某个服务ip挂掉后自动删除
     *
     * @param serviceName    服务名(接口名_版本号)
     * @param serviceAddress 服务地址
     */
    public void register(String serviceName, String serviceAddress) {
        // 创建本应用的register总节点 下边保存着各种服务的节点
        String registerRootPath = Constant.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registerRootPath)) {
            zkClient.createPersistent(registerRootPath);
            LOGGER.debug("创建所有服务注册的根节点:" + registerRootPath);
        }
        // 创建某一个服务的根节点
        String serviceRootPath = registerRootPath + "/" + serviceName;
        if (!zkClient.exists(serviceRootPath)) {
            zkClient.createPersistent(serviceRootPath);
            LOGGER.debug("创建某一服务的根节点:" + serviceRootPath);
        }
        // 创建某一个服务的某一个服务地址 节点类型：临时顺序节点
        // 临时节点只能作为叶子节点 需要有value  ip地址作为value
        String servicePath = serviceRootPath + "/address-";
        String ephemeralSequential = zkClient.createEphemeralSequential(servicePath, serviceAddress);
        LOGGER.debug("创建服务地址" + ephemeralSequential);
    }


}
