package org.example.ringetty.web;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RestMappingHandler {

    private Map<String, Method> MAPPING_MAP = new ConcurrentHashMap<>();

    public void init() {
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation("org.example.ringetty", Rest.class);
        classes.forEach(aClass -> {
            Rest typeRest = aClass.getAnnotation(Rest.class);
            List<Method> methods = ReflectUtil.getPublicMethods(aClass, method -> method.getAnnotation(Rest.class) != null);
            methods.forEach(method -> {
                Rest methodRest = method.getAnnotation(Rest.class);
            });
        });
    }

//    private

}
