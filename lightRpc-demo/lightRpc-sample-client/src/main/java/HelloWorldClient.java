import com.rpc.client.RpcProxy;
import com.rpc.sample.hello.HelloWorldService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description: 客户端调用
 * @Author: Jingzeng Wang
 * @Date: Created in 20:07  2017/9/2.
 */
public class HelloWorldClient {
    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        // 获得rpc代理类
        HelloWorldService helloService = rpcProxy.create(HelloWorldService.class);
        // 通过代理-远程调用服务获得执行结果
        String result = helloService.helloWorld("我是rpc！");
        System.out.println(result);
        //带有版本号的调用  服务要对应此version
/*        HelloWorldService helloService2 = rpcProxy.create(HelloWorldService.class, "sample.hello2");
        String result2 = helloService2.helloWorld("世界");
        System.out.println(result2);*/
    }
}
