package com.rpc.common.Serialization;

import com.rpc.common.bean.RpcRequest;
import org.junit.Test;

/**
 * @Description:
 * @Author: Jingzeng Wang
 * @Date: Created in 15:25  2017/8/21.
 */
public class ProtostufUtilsTest {
    @Test
    public void serializer() throws Exception {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId("11");
        rpcRequest.setInterfaceName("dasda");
        rpcRequest.setMethodName("dsada");
        rpcRequest.setServiceVersion("22");
        byte[] serializer = ProtostufUtils.serializerByProtostuff(rpcRequest);
        System.out.println(serializer);
        RpcRequest rpcRequest1 = ProtostufUtils.deserializerByProtostuff(serializer, RpcRequest.class);
        System.out.println(rpcRequest1.getRequestId());
    }

}