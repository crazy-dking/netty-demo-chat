package cn.itcast.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 这个位置主要是通过继承，自己封装一个LengthFieldBasedFrameDecoder参数
 * 防止篡改
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
