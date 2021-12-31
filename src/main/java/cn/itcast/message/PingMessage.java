package cn.itcast.message;

import lombok.Data;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2021/12/31
 */
@Data
public class PingMessage extends Message{
    @Override
    public int getMessageType() {
        return 0;
    }
}
