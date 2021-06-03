package org.example.ringetty.handler;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.lmax.disruptor.EventHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.ringetty.dsl.DisruptorManager;
import org.example.ringetty.event.HttpExchangeEvent;

import java.nio.charset.StandardCharsets;

public class SendHandler implements EventHandler<HttpExchangeEvent> {

    private Log log = LogFactory.get();

    public SendHandler() {
        DisruptorManager.sendHandleEventWith(this);
    }

    @Override
    public void onEvent(HttpExchangeEvent httpExchangeEvent, long l, boolean b) throws Exception {
        log.info("send http");

        HttpExchange httpExchange = httpExchangeEvent.getHttpExchange();
        byte[] bytes = (Thread.currentThread().getName() + ": SUCCESS").getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, bytes.length);
        httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
        httpExchange.getResponseBody().write(bytes);

        httpExchangeEvent.onCompleted();
    }

}
