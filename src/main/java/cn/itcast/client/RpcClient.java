package cn.itcast.client;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.server.ChatServer;
import cn.itcast.server.SequenceGenerator;
import cn.itcast.server.handler.RpcResponseMessageHandler;
import cn.itcast.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @Description:
 * 主要用来发送数据包到
 * @Author: wangdakai
 * @Date: 2022/1/4
 */
@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        // 将创建的channel抽取成一个单利模式。
        // 将创建对象通过代理进行解决
        HelloService helloService = proxy(HelloService.class);
        System.out.println(helloService.sayHello("王大凯"));
        System.out.println(helloService.sayHello("zj"));
        System.out.println(helloService.sayHello("dk"));
    }

    private static Channel channel = null ;
    private static Object lock = new Object();
    public static Channel getChannel(){
        if(channel!=null) {
            return channel;
        }
        synchronized (lock){
            if (channel == null) {
                initChannel();
            }
            return channel;
        }
    }
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable messageHandler = new MessageCodecSharable();
        RpcResponseMessageHandler rpcHandler = new RpcResponseMessageHandler();
        try{
            ChannelFuture connect = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(loggingHandler)
                                    .addLast(messageHandler)
                                    .addLast(rpcHandler);
                        }
                    })
                    .connect("localhost", 8080);
            channel = connect.sync().channel();
            channel.closeFuture().addListener(future -> group.shutdownGracefully());
        }catch (Exception e){
            log.info("error: {}",e.getMessage());
        }
    }

    public static <T> T proxy (Class<T> tClass){
        // 类加载器
        ClassLoader loader = tClass.getClassLoader();
        // 接口数组
        Class[] interfaceClasses = new Class[]{tClass};
        Object proxyInstance = Proxy.newProxyInstance(loader, interfaceClasses, (proxy, method, args) -> {
            Integer sequenceID = SequenceGenerator.getSequenceID();
            RpcRequestMessage message = new RpcRequestMessage(sequenceID,
                    tClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);
            Channel channel = getChannel();
            channel.writeAndFlush(message);
            DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceID,promise);
            /// await抛出异常。sync抛出异常
            promise.await();
            if (promise.isSuccess()) {
               return promise.getNow();

            }else {
                log.info("promise 异常{}",promise.cause());
            }
            return null;
        });
        return (T) proxyInstance;
    }
}
