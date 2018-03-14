package com.rpc.server.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 服务注解类 可被spring扫描  作用在服务实现类上
 * @Author: Jingzeng Wang
 * @Date: Created in 20:45  2017/8/20.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface LightRpcService {

    //服务接口
    Class<?> value();

    //服务版本号
    String version() default "";
}
