package cn.itcast.server;

import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProtocolFrameDecoder;
import cn.itcast.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        // 不用每次都创建
        LoginRequestMessageHandler loginRequestMessageHandler = new LoginRequestMessageHandler();
        ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler();
        GroupChatRequestMessageHandler groupChatRequestMessageHandler = new GroupChatRequestMessageHandler();
        GroupCreateRequestMessageHandler groupCreateRequestMessageHandler = new GroupCreateRequestMessageHandler();
        GroupJoinRequestMessageHandler groupJoinRequestMessageHandler = new GroupJoinRequestMessageHandler();
        GroupMembersRequestMessageHandler groupMembersRequestMessageHandler = new GroupMembersRequestMessageHandler();
        GroupQuitRequestMessageHandler groupQuitRequestMessageHandler = new GroupQuitRequestMessageHandler();
        QuitHandler quitHandler = new QuitHandler();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 空闲状态检测器，来判断是否连接假死 读空闲，写空闲，读写空闲时间
                    // 5秒内有收到 接收不到就会触发一个IdleState.READER_IDLE时间。我们需要自己写一个handler监控这个事件
                    ch.pipeline().addLast(new IdleStateHandler(5,0,0));

                    // ChannelDuplexHandler 双向的
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        // 触发自定义事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent evt1 = (IdleStateEvent) evt;
                            // 如果我们监听到的状态是
                            if(evt1.state()== IdleState.READER_IDLE){
                                log.info("5秒没有读取到数据");
                                ctx.channel().close();
                            }

                        }
                    });
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 像这样每个我们都创建内部类代码很长很难维护。我们需要见匿名内部类提取成内部类，在提取成类。独立的逻辑方便维护
                    ch.pipeline().addLast(loginRequestMessageHandler);
                    ch.pipeline().addLast(chatRequestMessageHandler);
                    ch.pipeline().addLast(groupChatRequestMessageHandler);
                    ch.pipeline().addLast(groupCreateRequestMessageHandler);
                    ch.pipeline().addLast(groupJoinRequestMessageHandler);
                    ch.pipeline().addLast(groupMembersRequestMessageHandler);
                    ch.pipeline().addLast(groupQuitRequestMessageHandler);
                    ch.pipeline().addLast(quitHandler);
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
