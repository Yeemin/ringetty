package org.example.ringetty.handler;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.example.ringetty.event.HttpExchangeEvent;
import org.example.ringetty.event.HttpExchangeEventFactory;

public class ReceiveHandler implements EventHandler<HttpExchangeEvent> {

    private Log log = LogFactory.get();

    private final Disruptor<HttpExchangeEvent> disruptor = new Disruptor<>(new HttpExchangeEventFactory(),
            1024, new NamedThreadFactory("dispatcher-", false));

    public ReceiveHandler() {
        this.disruptor.handleEventsWith(new DispatcherHandler());
        this.disruptor.start();
    }

    @Override
    public void onEvent(HttpExchangeEvent httpExchangeEvent, long l, boolean b) throws Exception {
        this.disruptor.publishEvent(((event, sequence) -> {
            event.setHttpExchange(httpExchangeEvent.getHttpExchange());
            event.setCountDownLatch(httpExchangeEvent.getCountDownLatch());
        }));
    }
}
