package org.example.ringetty.web;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RestMappingHandler {

    private static Map<String, Method> MAPPING_MAP = new ConcurrentHashMap<>();
    private static Map<Method, Object> REST_MAP = new ConcurrentHashMap<>();
    private static Map<Method, Class<?>> PARAM_TYPE_MAP = new ConcurrentHashMap<>();

    public static void init() {
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation("org.example.ringetty", Rest.class);
        classes.forEach(aClass -> {
            Object object = null;
            try {
                object = aClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            Rest typeRest = aClass.getAnnotation(Rest.class);
            List<Method> methods = ReflectUtil.getPublicMethods(aClass, method -> method.getAnnotation(Rest.class) != null);
            Object finalObject = object;
            methods.forEach(method -> {
                Rest methodRest = method.getAnnotation(Rest.class);
                List<String> urls = generateUrls(typeRest.value(), methodRest.value());
                urls.forEach(url -> MAPPING_MAP.put(url, method));
                REST_MAP.put(method, finalObject);
                Arrays.stream(method.getParameterTypes()).filter(type -> type != HttpExchange.class)
                        .findFirst().ifPresent(type -> PARAM_TYPE_MAP.put(method, type));
            });
        });
    }

    public static Method getMappedMethod(String path) {
        return MAPPING_MAP.get(path);
    }

    public static Object getRestBean(Method method) {
        return REST_MAP.get(method);
    }

    public static Class<?> getParamType(Method method) {
        return PARAM_TYPE_MAP.get(method);
    }

    private static List<String> generateUrls(String[] prefixs, String[] sulfixs) {
        List<String> list = new ArrayList<>();
        String url;
        for (String sulfix : sulfixs) {
            if (!sulfix.startsWith("/")) {
                sulfix = "/" + sulfix;
            }
            if (sulfix.endsWith("/")) {
                sulfix = sulfix.substring(0, sulfix.length() - 1);
            }
            url = sulfix;
            for (String prefix : prefixs) {
                if (!prefix.startsWith("/")) {
                    prefix = "/" + prefix;
                }
                if (prefix.endsWith("/")) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                }
                url = prefix + sulfix;
            }
            list.add(url);
        }

        return list;
    }

}
