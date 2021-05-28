package org.example.ringetty.event;

import com.lmax.disruptor.EventFactory;

public class HttpExchangeEventFactory implements EventFactory<HttpExchangeEvent> {

    @Override
    public HttpExchangeEvent newInstance() {
        return new HttpExchangeEvent();
    }
}
