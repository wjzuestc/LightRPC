package com.rpc.register.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Description: 实现客户端对服务的自动发现
 * @Author: Jingzeng Wang
 * @Date: Created in 16:30  2017/8/21.
 */
public class ZookeeperDiscoverService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegisterService.class);

    private String zkRegisterAddress;

    public ZookeeperDiscoverService(String zkRegisterAddress) {
        this.zkRegisterAddress = zkRegisterAddress;
    }

    /**
     * 完成对服务的查找与路由策略
     *
     * @param Servicename 服务名
     * @return
     */
    public String discover(String Servicename) {
        ZkClient zkClient = new ZkClient(zkRegisterAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.info("连接zookeeper!");
        try {
            String servicePath = Constant.ZK_REGISTRY_PATH + "/" + Servicename;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException("服务路径不存在!");
            }
            List<String> serviceAddress = zkClient.getChildren(servicePath);
            if (serviceAddress.isEmpty()) {
                throw new RuntimeException("无可用服务!");
            }
            //注册是的路径：regsiter/service/address-sequence(value)
            String address;
            if (serviceAddress.size() == 1) {
                // 若只有一个地址，则获取该地址
                address = serviceAddress.get(0);
                LOGGER.info("只有一个服务地址:" + servicePath + "/" + address);
            } else {
                //一个服务有多个服务地址时，利用随机分类的路由策略进行分配服务
                address = serviceAddress.get(ThreadLocalRandom.current().nextInt(serviceAddress.size()));
                LOGGER.info("获得任意一个服务地址:" + servicePath + "/" + address);
            }
            //读取服务地址
            return zkClient.readData(servicePath + "/" + address);
        } finally {
            zkClient.close();
        }
    }
}
