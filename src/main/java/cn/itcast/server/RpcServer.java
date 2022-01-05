package cn.itcast.server;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProtocolFrameDecoder;
import cn.itcast.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2021/12/31
 */
@Slf4j
public class RpcServer {
    public static void main(String[] args) {

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable sharable = new MessageCodecSharable();
        RpcRequestMessageHandler rpcHandler = new RpcRequestMessageHandler();

        try {
            ChannelFuture bind = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 防止粘包，半包。自己封装的处理器
                            ch.pipeline().addLast(new ProtocolFrameDecoder())
                                    .addLast(loggingHandler)
                                    .addLast(sharable)
                                    .addLast(rpcHandler);

                        }
                    })
                    .bind(8080);
            Channel channel = bind.sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("server error",e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
