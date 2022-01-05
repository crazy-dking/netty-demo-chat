package cn.itcast.message;

import lombok.Data;
import lombok.ToString;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2021/12/31
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message{
    private Object returnValue;
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
