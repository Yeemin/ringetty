package org.example.ringetty.server;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.setting.Setting;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import com.sun.net.httpserver.HttpServer;
import org.example.ringetty.event.HttpExchangeEvent;
import org.example.ringetty.event.HttpExchangeEventFactory;
import org.example.ringetty.handler.ReceiveHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RingHttpServer {

    private final Disruptor<HttpExchangeEvent> disruptor = new Disruptor<>(new HttpExchangeEventFactory(),
            1024, new NamedThreadFactory("receive-", false));

    public RingHttpServer(final Setting setting) throws IOException {
        Integer port = setting.getInt("server.port", 8080);
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        AtomicInteger threadIndex = new AtomicInteger(0);
        Integer threadSize = setting.getInt("server.thread.size", 2048);
        ThreadGroup threadGroup = new ThreadGroup("http");
        ExecutorService executor = Executors.newFixedThreadPool(threadSize, r ->
                new Thread(threadGroup, r, "http-exec-" + threadIndex.incrementAndGet())
        );
        httpServer.setExecutor(executor);

        this.disruptor.handleEventsWith(new ReceiveHandler());
        this.disruptor.start();

        httpServer.createContext("/", httpExchange -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            disruptor.publishEvent(new EventTranslator<HttpExchangeEvent>() {
                @Override
                public void translateTo(HttpExchangeEvent event, long sequence) {
                    event.setCountDownLatch(countDownLatch);
                    event.setHttpExchange(httpExchange);
                }
            });
        });

        httpServer.start();
    }

}
