package cn.itcast.client;

import cn.itcast.message.*;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean login = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 检测写空闲，如果3秒内咩有向服务器写，我们就手动写一个过去
                    ch.pipeline().addLast(new IdleStateHandler(0,3,0));
                    // ChannelDuplexHandler 双向的
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        // 触发自定义事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent evt1 = (IdleStateEvent) evt;
                            // 如果我们监听到的状态是
                            if(evt1.state()== IdleState.WRITER_IDLE){
                                log.info("3秒，自动心跳");
                                ctx.channel().writeAndFlush(new PingMessage());
                            }

                        }
                    });
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            // 用来接受来自server的信息
                            log.info("msg:{}",msg);
                            LoginResponseMessage response = (LoginResponseMessage) msg;
                            if (response.isSuccess()){
                                login.set(true);
                            }

                            // 当我们响应回来我们要进行调用system in线程。可以通过阻塞队列，wait lock等打法，countDownLunch
                            countDownLatch.countDown();
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 检测客户端连接到server，创建一个线程去登录
                            new Thread(() -> {
                                Scanner scan = new Scanner(System.in);
                                System.out.println("请输入用户名");
                                String userName = scan.nextLine();
                                System.out.println("其请输入密码");
                                String pwd = scan.nextLine();
                                ctx.writeAndFlush(new LoginRequestMessage(userName,pwd));
                                System.out.println("等待后续操作");
                                try {
                                    //需要等到我们的count减到1才可以继续进行。
                                    countDownLatch.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (login.get()) {
                                    while(true){
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");
                                        String command = scan.nextLine();
                                        // 获得指令及其参数，并发送对应类型消息
                                        String[] commands = command.split(" ");
                                        switch (commands[0]){
                                            case "send":
                                                ctx.writeAndFlush(new ChatRequestMessage(userName, commands[1], commands[2]));
                                                break;
                                             case "gsend":
                                                ctx.writeAndFlush(new GroupChatRequestMessage(userName,commands[1], commands[2]));
                                                break;
                                            case "gcreate":
                                                // 分割，获得群员名
                                                String[] members = commands[2].split(",");
                                                Set<String> set = new HashSet<>(Arrays.asList(members));
                                                // 把自己加入到群聊中
                                                set.add(userName);
                                                ctx.writeAndFlush(new GroupCreateRequestMessage(commands[1],set));
                                                break;
                                            case "gmembers":
                                                ctx.writeAndFlush(new GroupMembersRequestMessage(commands[1]));
                                                break;
                                            case "gjoin":
                                                ctx.writeAndFlush(new GroupJoinRequestMessage(userName, commands[1]));
                                                break;
                                            case "gquit":
                                                ctx.writeAndFlush(new GroupQuitRequestMessage(userName, commands[1]));
                                                break;
                                            case "quit":
                                                ctx.channel().close();
                                                return;
                                            default:
                                                System.out.println("指令有误，请重新输入");
                                                continue;
                                        }
                                    }
                                }else{
                                    // 登录失败
                                    ctx.channel().close();
                                }
                            },"system in").start();
                            super.channelActive(ctx);
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
