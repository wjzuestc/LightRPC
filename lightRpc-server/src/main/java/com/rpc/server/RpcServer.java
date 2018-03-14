package com.rpc.server;

import com.rpc.common.bean.RpcRequest;
import com.rpc.common.bean.RpcResponse;
import com.rpc.common.codec.RpcDecoder;
import com.rpc.common.codec.RpcEncoder;
import com.rpc.register.zookeeper.ZookeeperRegisterService;
import com.rpc.server.annotation.LightRpcService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 完成rpc服务调用
 * @Author: Jingzeng Wang
 * @Date: Created in 21:10  2017/8/20.
 * @update: 2017/9/1 By Wjz
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private ZookeeperRegisterService zookeeperRegisterService;
    private String rpcServiceAddress;

    /**
     * 存放服务接口名与实例bean的映射
     */
    private Map<String, Object> serviceMap = new HashMap<String, Object>();

    public RpcServer(ZookeeperRegisterService zookeeperRegisterService, String rpcServiceAddress) {
        this.zookeeperRegisterService = zookeeperRegisterService;
        this.rpcServiceAddress = rpcServiceAddress;
    }

    /**
     * 扫描带有 RpcService 注解的类并初始化 handlerMap 对象
     * 在初始化bean是调用
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 返回带有LightRpcService注解的bean名与bean实例，并将其放入map中
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(LightRpcService.class);
        if (MapUtils.isNotEmpty(beanMap)) {
            // 遍历所有带有注解的bean实例，获取其接口名及服务实现的版本号
            for (Object serviceBean : beanMap.values()) {
                LightRpcService rpcAnnotation = serviceBean.getClass().getAnnotation(LightRpcService.class);
                String interfaceName = rpcAnnotation.value().getName();
                String serviceVersion = rpcAnnotation.version();
                if (StringUtils.isNotEmpty(serviceVersion)) {
                    interfaceName += "-" + serviceVersion;
                }
                // 将<接口名_版本号, 接口实现类bean> 放入serviceMap中，供客户端查找调用
                serviceMap.put(interfaceName, serviceBean);
            }
        }
    }

    /**
     * Netty 连接zk，并接收客户端服务请求进行处理
     * 在服务端初始化bean时调用
     * Netty 经典写法
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //有客户端连接后进行
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))       //处理入站请求解码(反序列化过程)
                                    .addLast(new RpcEncoder(RpcResponse.class))       //处理出站响应编码(序列化过程)
                                    .addLast(new RpcServiceHandler(serviceMap));      //完成服务调用
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)     // TCP连接数
                    .option(ChannelOption.SO_KEEPALIVE, true);  // 长连接
            //获取rpc服务的服务器地址
            String[] split = StringUtils.split(rpcServiceAddress, ":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);
            //同步绑定监听端口端口
            ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();
            //将服务注册到zk上
            if (zookeeperRegisterService != null) {
                // 遍历待注册服务的接口名_版本号+服务地址
                for (String serviceName : serviceMap.keySet()) {
                    zookeeperRegisterService.register(serviceName, rpcServiceAddress);
                    LOGGER.info("注册完成！" + serviceName + rpcServiceAddress);
                }
            } else {
                LOGGER.error("zookeeperRegisterService为null！无法完成服务的注册！");
            }
            LOGGER.info("服务已启动，监听端口为：" + port);
            //等待结束
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
