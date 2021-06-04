package org.example.ringetty.handler;

import cn.hutool.core.io.IoUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.lmax.disruptor.EventHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.ringetty.dsl.DisruptorManager;
import org.example.ringetty.event.HttpExchangeEvent;
import org.example.ringetty.web.RestMappingHandler;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class DispatcherHandler implements EventHandler<HttpExchangeEvent> {

    private Log log = LogFactory.get();

    public DispatcherHandler() {
        DisruptorManager.dispatchHandleEventWith(this);
    }

    Gson gson = new GsonBuilder().create();

    @Override
    public void onEvent(HttpExchangeEvent httpExchangeEvent, long l, boolean b) throws Exception {

        log.info("dispatch http");
        HttpExchange httpExchange = httpExchangeEvent.getHttpExchange();
        String path = httpExchange.getRequestURI().getPath();
        Method method = RestMappingHandler.getMappedMethod(path);
        String jsonString = IoUtil.read(httpExchange.getRequestBody(), Charset.defaultCharset());
        Object data = gson.fromJson(jsonString, RestMappingHandler.getParamType(method));
        Object object = method.invoke(RestMappingHandler.getRestBean(method), data);
        httpExchangeEvent.setObject(object);
        DisruptorManager.send(httpExchangeEvent);
    }
}
