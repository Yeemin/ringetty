package org.example.ringetty.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class RingHttpServer {

    private final HttpServer httpServer;

    public static RingHttpServer create(int port) throws IOException {
        return new RingHttpServer(port);
    }

    public static RingHttpServer create() throws IOException {
        return new RingHttpServer();
    }

    private RingHttpServer() throws IOException {
        this.httpServer = HttpServer.create();
    }

    private RingHttpServer(int port) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public RingHttpServer port(int port) throws IOException {
        this.httpServer.bind(new InetSocketAddress(port), 0);
        return this;
    }

    public RingHttpServer executor(Executor executor) {
        this.httpServer.setExecutor(executor);
        return this;
    }

    public RingHttpServer createContext(String path, HttpHandler httpHandler) {
        this.httpServer.createContext(path, httpHandler);
        return this;
    }

    public void start() {
        this.httpServer.start();
    }

    public void stop(int delay) {
        this.httpServer.stop(delay);
    }




}
