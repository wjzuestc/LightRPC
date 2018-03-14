package com.rpc.client;

import com.rpc.common.bean.RpcRequest;
import com.rpc.common.bean.RpcResponse;
import com.rpc.common.codec.RpcDecoder;
import com.rpc.common.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 连接生产者，调用rpc服务
 * @Author: Jingzeng Wang
 * @Date: Created in 19:13  2017/9/21.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    RpcResponse rpcResponse;

    /**
     * 服务提供者ip
     */
    private final String host;

    /**
     * 服务提供者端口
     */
    private final int port;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 连接服务提供端，调用服务，返回结果
     *
     * @param rpcRequest
     * @return
     */
    public RpcResponse send(RpcRequest rpcRequest) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new RpcEncoder(RpcRequest.class));  // 出栈--对请求编码 序列化
                            pipeline.addLast(new RpcDecoder(RpcResponse.class)); // 入站--对响应解码 反序列化
                            pipeline.addLast(RpcClient.this);   // 处理逻辑
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true);
            // 连接服务提供端
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // 写入 RPC 请求数据并关闭连接
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(rpcRequest).sync();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("netty连接出现错误!");
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
        }
        // 返回 RPC 响应对象
        return rpcResponse;
    }

    /**
     * 返回服务调用结果
     *
     * @param channelHandlerContext
     * @param rpcResponse
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.rpcResponse = rpcResponse;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("服务调用exception", cause);
        ctx.close();
    }
}
