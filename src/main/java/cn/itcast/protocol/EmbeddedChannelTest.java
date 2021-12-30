package cn.itcast.protocol;

import cn.itcast.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @author: zjdking
 * @date: 2021/12/30 21:16
 */
@Slf4j
public class EmbeddedChannelTest {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                // 防止粘包半包，所以我们通过真解码器进行解决
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(),
                new MessageCodecSharable()
        );
        // encode
        LoginRequestMessage zhangsan = new LoginRequestMessage("zhangsan", "123");
        embeddedChannel.writeInbound(zhangsan);

        // decode
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,zhangsan,buffer);

//        embeddedChannel.writeInbound(buffer);
        ByteBuf slice = buffer.slice(0, 100);
        ByteBuf slice1 = buffer.slice(100, buffer.readableBytes() - 100);
        slice.retain();
        slice1.retain();

        // 如果只读这个一个就会出现半包。 他们执行完成会被release,所以我们上边要进行retain
        embeddedChannel.writeInbound(slice);
        embeddedChannel.writeInbound(slice1);
    }

}
