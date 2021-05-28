package org.example.ringetty.http;

import com.sun.net.httpserver.HttpExchange;

public class HttpExchangeEvent {

    HttpExchange httpExchange;

    public HttpExchangeEvent() {
    }

    public HttpExchangeEvent(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }
}
