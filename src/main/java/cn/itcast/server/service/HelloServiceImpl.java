package cn.itcast.server.service;

/**
 * @Description:
 * @Author: wangdakai
 * @Date: 2022/1/4
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String m) {
        int i = 0;
        int i1 = 9 / i;
        return "hello"+m;
    }
}
