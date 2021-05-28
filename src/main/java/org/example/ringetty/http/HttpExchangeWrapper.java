package org.example.ringetty.http;

import com.sun.net.httpserver.HttpExchange;

public class HttpExchangeWrapper {

    HttpExchange httpExchange;

    public HttpExchangeWrapper() {
    }

    public HttpExchangeWrapper(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }
}
