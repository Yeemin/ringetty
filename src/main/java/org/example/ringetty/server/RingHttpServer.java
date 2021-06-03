package org.example.ringetty.server;

import cn.hutool.setting.Setting;
import com.sun.net.httpserver.HttpServer;
import org.example.ringetty.dsl.DisruptorManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RingHttpServer {

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
        httpServer.createContext("/", httpExchange -> {
            DisruptorManager.receive(httpExchange, new CountDownLatch(1));
        });
        DisruptorManager.startAll();
        httpServer.start();
    }

}
