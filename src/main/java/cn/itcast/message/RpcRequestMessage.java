package cn.itcast.message;

import lombok.Getter;
import lombok.ToString;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2021/12/31
 */
@Getter
@ToString(callSuper = true)
public class RpcRequestMessage extends Message{
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 返回值类型
     */
    private Class<?> returnType;
    /**
     * 参数类型
     */
    private Class[] parameterTypes;
    /**
     * 参数值
     */
    private Object[] parameterValue;

    public RpcRequestMessage(int sequenceId,String interfaceName, String methodName, Class<?> returnType, Class[] parameterTypes,
                             Object[] parameterValue) {
        super.setSequenceId(sequenceId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
