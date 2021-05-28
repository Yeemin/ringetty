package org.example.ringetty.handler;

import com.lmax.disruptor.EventHandler;
import org.example.ringetty.http.HttpExchangeWrapper;

public class ReceiveHandler implements EventHandler<HttpExchangeWrapper> {
    @Override
    public void onEvent(HttpExchangeWrapper httpExchangeWrapper, long l, boolean b) throws Exception {

    }
}
