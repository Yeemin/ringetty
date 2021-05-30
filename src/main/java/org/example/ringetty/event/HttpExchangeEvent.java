package org.example.ringetty.event;

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

    public void setHttpExchange(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }
}
