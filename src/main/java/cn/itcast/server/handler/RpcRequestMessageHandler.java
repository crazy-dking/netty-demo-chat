package cn.itcast.server.handler;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.message.RpcResponseMessage;
import cn.itcast.server.service.HelloService;
import cn.itcast.server.service.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2022/1/4
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) throws Exception {
        // 服务端进行监控我们的请求。
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId());
        try{
            HelloService service = (HelloService) ServiceFactory.getService(Class.forName(message.getInterfaceName()));
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            Object object = method.invoke(service, message.getParameterValue());
            response.setReturnValue(object);

        }catch (Exception e){
            e.printStackTrace();
            log.info(e.getMessage());
            response.setExceptionValue(e);
        }
        ctx.writeAndFlush(response);
    }

    public static void main(String[] args) throws Exception {
        RpcRequestMessage message = new RpcRequestMessage(1, "cn.itcast.server.service.HelloService"
                , "sayHello", String.class, new Class[]{String.class}, new Object[]{"zjdking"});
        HelloService service = (HelloService) ServiceFactory.getService(Class.forName(message.getInterfaceName()));
        Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        Object object = method.invoke(service, message.getParameterValue());


        System.out.println(object.toString());
    }
}
