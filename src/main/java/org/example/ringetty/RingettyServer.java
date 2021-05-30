package org.example.ringetty;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.example.ringetty.event.HttpExchangeEvent;
import org.example.ringetty.event.HttpExchangeEventFactory;
import org.example.ringetty.server.RingHttpServer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class RingettyServer {

    public RingettyServer(int port) throws IOException {
        AtomicInteger receiveIndex = new AtomicInteger(1);

        Disruptor<HttpExchangeEvent> receiveDisruptor = new Disruptor<>(new HttpExchangeEventFactory(),
                2048,
                r -> {
                    return new Thread(r, "receive-handler-" + receiveIndex.getAndIncrement());
                });


        AtomicInteger handlerIndex = new AtomicInteger(1);
        RingHttpServer.create(port)
                .executor(Executors.newFixedThreadPool(1024, new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "ringetty-http-" + handlerIndex.getAndIncrement());
                    }
                }))
                .createContext("/", exchange -> {
                    RingBuffer<HttpExchangeEvent> ringBuffer = receiveDisruptor.getRingBuffer();
                    long next = ringBuffer.next();
                    HttpExchangeEvent httpExchangeEvent = ringBuffer.get(next);
                    httpExchangeEvent.setHttpExchange(exchange);
                    ringBuffer.publish(next);
                }).start();
    }

}
