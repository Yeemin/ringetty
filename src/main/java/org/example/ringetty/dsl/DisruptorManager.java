package org.example.ringetty.dsl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.sun.net.httpserver.HttpExchange;
import org.example.ringetty.event.HttpExchangeEvent;
import org.example.ringetty.event.HttpExchangeEventFactory;
import org.example.ringetty.handler.DispatcherHandler;
import org.example.ringetty.handler.ReceiveHandler;
import org.example.ringetty.handler.SendHandler;

import java.util.concurrent.CountDownLatch;

public class DisruptorManager {

    private static Log log = LogFactory.get();

    private final static Disruptor<HttpExchangeEvent> RECEIVE_DISRUPTOR = new Disruptor<>(new HttpExchangeEventFactory(),
            1024, new NamedThreadFactory("receive-", false));

    private final static Disruptor<HttpExchangeEvent> DISPATCH_DISRUPTOR = new Disruptor<>(new HttpExchangeEventFactory(),
            2048, new NamedThreadFactory("dispatcher-", false));

    private final static Disruptor<HttpExchangeEvent> SEND_DISRUPTOR = new Disruptor<>(new HttpExchangeEventFactory(),
            1024, new NamedThreadFactory("send-", false));

    public static void receive(final HttpExchange httpExchange, final CountDownLatch countDownLatch) {
        RECEIVE_DISRUPTOR.publishEvent(((event, sequence) -> {
            event.setHttpExchange(httpExchange);
            event.setCountDownLatch(countDownLatch);
        }));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    public static void startAll() {
        new ReceiveHandler();
        new DispatcherHandler();
        new SendHandler();
        RECEIVE_DISRUPTOR.start();
        DISPATCH_DISRUPTOR.start();
        SEND_DISRUPTOR.start();
    }

    public static void receiveHandleEventWith(EventHandler<HttpExchangeEvent> eventHandler) {
        RECEIVE_DISRUPTOR.handleEventsWith(eventHandler);
    }

    public static void dispatch(HttpExchangeEvent httpExchangeEvent) {
        DISPATCH_DISRUPTOR.publishEvent(((event, sequence) -> {
            event.setHttpExchange(httpExchangeEvent.getHttpExchange());
            event.setCountDownLatch(httpExchangeEvent.getCountDownLatch());
        }));
    }

    public static void dispatchHandleEventWith(EventHandler<HttpExchangeEvent> eventHandler) {
        DISPATCH_DISRUPTOR.handleEventsWith(eventHandler);
    }

    public static void send(HttpExchangeEvent httpExchangeEvent) {
        SEND_DISRUPTOR.publishEvent(((event, sequence) -> {
            event.setHttpExchange(httpExchangeEvent.getHttpExchange());
            event.setCountDownLatch(httpExchangeEvent.getCountDownLatch());
        }));
    }

    public static void sendHandleEventWith(EventHandler<HttpExchangeEvent> eventHandler) {
        SEND_DISRUPTOR.handleEventsWith(eventHandler);
    }


}
