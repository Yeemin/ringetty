package org.example.ringetty.handler;

import com.lmax.disruptor.EventHandler;
import org.example.ringetty.http.HttpExchangeEvent;

public class SendHandler implements EventHandler<HttpExchangeEvent> {

    @Override
    public void onEvent(HttpExchangeEvent httpExchangeWrapper, long l, boolean b) throws Exception {

    }

}
