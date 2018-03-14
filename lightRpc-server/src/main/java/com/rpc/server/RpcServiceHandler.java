package com.rpc.server;

import com.rpc.common.bean.RpcRequest;
import com.rpc.common.bean.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Description: 完成服务的调用
 * @Author: Jingzeng Wang
 * @Date: Created in 11:24  2017/8/21.
 */
public class RpcServiceHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RpcServiceHandler.class);

    /**
     * map结构：<服务接口名_服务版本号,服务实现类bean></>
     */
    private final Map<String, Object> handlermap;

    public RpcServiceHandler(Map<String, Object> handlermap) {
        this.handlermap = handlermap;
    }

    /**
     * 读回调函数  进行服务调用
     *
     * @param channelHandlerContext
     * @param rpcRequest            调用链完成反序列化客户端的调用请求封装到rpcRequest中
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 服务端的响应封装类
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            Object result = handler(rpcRequest);
            rpcResponse.setResult(result);
        } catch (Exception e) {
            LOGGER.error("调用服务出现异常！");
            rpcResponse.setException(e);
        }
        // 写入 RPC 响应对象并自动关闭连接  进入下游序列化链
        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 反射调用服务
     *
     * @param rpcRequest 客户端请求封装
     * @return
     */
    private Object handler(RpcRequest rpcRequest) throws Exception {
        String interfaceName = rpcRequest.getInterfaceName();
        String version = rpcRequest.getServiceVersion();
        if (StringUtils.isNotEmpty(version)) {
            interfaceName += "-" + version;
        }
        // 获取此接口的服务实现类bean
        Object serviceBean = handlermap.get(interfaceName);
        if (serviceBean == null) {
            throw new RuntimeException("没找到此服务" + interfaceName);
        }
        //获得反射调用的参数
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        //Java反射调用服务器端的服务实现方法  也可以使用更高性能的cglib反射
        Method method = serviceBean.getClass().getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object invokeResult = method.invoke(serviceBean, parameters);
        return invokeResult;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
