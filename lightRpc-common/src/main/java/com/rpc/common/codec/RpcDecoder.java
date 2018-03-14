package com.rpc.common.codec;

import com.rpc.common.Serialization.ProtostufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Description: 服务端对客户端的请求解码，客户端对服务器的响应解码
 * @Author: Jingzeng Wang
 * @Date: Created in 10:58  2017/8/21.
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private final static Logger LOGGER = LoggerFactory.getLogger(RpcEncoder.class);

    /**
     * 解码类型
     */
    private Class<?> baseClass;

    public RpcDecoder(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    /**
     * 解码方法  具体要根据编码方式来进行解码：长度+byte消息
     * 处理链上进行调用--编码--进栈的时候调用
     * 解码规则：长度+byte数据
     *
     * @param channelHandlerContext
     * @param byteBuf               消息
     * @param list                  输出
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //若消息不完整 因为至少也得有 长度
        if (byteBuf.readableBytes() < 4) {
            LOGGER.error("解码错误，消息长度小于4，不完整！");
            return;
        }
        // 把当前的readerIndex赋值到markReaderIndex中
        byteBuf.markWriterIndex();
        // 读取消息长度
        int length = byteBuf.readInt();
        // 若消息不完整
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            LOGGER.error("消息长度小于数据长度，未完成读取！");
            return;
        }
        // 读取消息体
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        // 反序列化
        Object o = ProtostufUtils.deserializerByProtostuff(bytes, baseClass);
        // 将反序列化的结果传到下游处理链
        list.add(o);
    }
}
