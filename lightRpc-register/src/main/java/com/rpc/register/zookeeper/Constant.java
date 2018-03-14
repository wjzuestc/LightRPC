package com.rpc.register.zookeeper;

/**
 * @Description: zookeeper 的参数常量类
 * @Author: Jingzeng Wang
 * @Date: Created in 21:41  2017/8/20.
 */
public final class Constant {
    /**
     * session会话超时时间
     */
    public final static int ZK_SESSION_TIMEOUT = 5000;

    /**
     * 连接超时时间
     */
    public final static int ZK_CONNECTION_TIMEOUT = 1000;

    /**
     * 服务注册根节点
     */
    public final static String ZK_REGISTRY_PATH = "/registry";
}
