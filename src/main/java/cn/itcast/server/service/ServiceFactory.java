package cn.itcast.server.service;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * 通过map进行获取指定的值。然后通过配置文件进行配置。通过反射获取得到中的service对象。
 *
 * @Author: wangdakai
 * @Date: 2022/1/4
 */
public class ServiceFactory {
    static Properties properties;
    static Map<Class<?>,Object> map = new ConcurrentHashMap<>();
    static{
        try(InputStream in  = ServiceFactory.class.getResourceAsStream("/application.properties")){
            properties = new Properties();
            properties.load(in);
            Set<String> strings = properties.stringPropertyNames();
            for (String string: strings) {
                if(string.endsWith("Service")){
                    Class<?> interClass = Class.forName(string);
                    Class<?> implClass = Class.forName(properties.getProperty(string));
                    map.put(interClass, implClass.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static <T> T getService(Class<T> interClass){
        return (T)map.get(interClass);
    }
}
