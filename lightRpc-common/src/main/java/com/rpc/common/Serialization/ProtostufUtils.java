package com.rpc.common.Serialization;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 利用protostuff进行序列化与反序列化 标准模板
 * @Author: Jingzeng Wang
 * @Date: Created in 11:22  2017/8/21.
 */
public class ProtostufUtils {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static <T> Schema<T> getSchema(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Schema<T> schema = (Schema<T>) cachedSchema.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.getSchema(clazz);
            if (schema != null) {
                cachedSchema.put(clazz, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化 将对象转化为字节数组
     * 获得对象的类  使用LinkedBuffer分配一块默认大小的buffer空间  通过对象的类构建对应的schema
     * 使用给定的schema将对象序列化为一个byte数组，并返回
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serializerByProtostuff(T obj) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化 将字节数组转为对象
     * 实例化一个类的对象 通过对象的类构建对应的schema 使用给定的schema将byte数组和对象合并，并返回
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserializerByProtostuff(byte[] data, Class<T> clazz) {
        try {
            T obj = clazz.newInstance();
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(data, obj, schema);
            return obj;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
