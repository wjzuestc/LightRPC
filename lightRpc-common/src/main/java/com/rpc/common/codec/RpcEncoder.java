package com.rpc.common.codec;

import com.rpc.common.Serialization.ProtostufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Description: 服务器对响应编码，客户端对请求编码
 * @Author: Jingzeng Wang
 * @Date: Created in 10:59  2017/8/21.
 */
public class RpcEncoder extends MessageToByteEncoder {

    /**
     * 编码类型
     */
    private Class<?> baseClass;

    public RpcEncoder(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    /**
     * 客户端对请求编码，服务端对响应编码
     * 处理链上进行调用--编码--出栈的时候调用
     * 编码规则：长度+byte数据
     *
     * @param channelHandlerContext
     * @param o                     编码对象
     * @param byteBuf               输出流
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (baseClass.isInstance(o)) {
            //编码规则：长度+对象属性数据值
            byte[] bytesBody = ProtostufUtils.serializerByProtostuff(o);
            int length = bytesBody.length;
            byteBuf.writeInt(length);
            byteBuf.writeBytes(bytesBody);
        }
    }
}
