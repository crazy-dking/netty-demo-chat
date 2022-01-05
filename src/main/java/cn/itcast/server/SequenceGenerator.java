package cn.itcast.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2022/1/5
 */
public abstract  class SequenceGenerator {
    private static final AtomicInteger num = new AtomicInteger();
    public static Integer getSequenceID(){
        return num.getAndIncrement();
    }
}
