package org.example.ringetty.event;

import com.sun.net.httpserver.HttpExchange;

import java.util.concurrent.CountDownLatch;

public class HttpExchangeEvent {

    private HttpExchange httpExchange;
    private CountDownLatch countDownLatch;
    private Object object;

    public HttpExchangeEvent() {
    }

    public HttpExchangeEvent(HttpExchange httpExchange, CountDownLatch countDownLatch) {
        this.httpExchange = httpExchange;
        this.countDownLatch = countDownLatch;
    }

    public void onCompleted() {
        this.countDownLatch.countDown();
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    public void setHttpExchange(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
