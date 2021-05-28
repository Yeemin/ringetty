package org.example.ringetty.handler;

import com.lmax.disruptor.EventHandler;
import org.example.ringetty.event.HttpExchangeEvent;

public class ReceiveHandler implements EventHandler<HttpExchangeEvent> {
    @Override
    public void onEvent(HttpExchangeEvent httpExchangeWrapper, long l, boolean b) throws Exception {

    }
}
