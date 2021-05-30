package org.example.ringetty.handler;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.lmax.disruptor.EventHandler;
import org.example.ringetty.event.HttpExchangeEvent;

public class SendHandler implements EventHandler<HttpExchangeEvent> {

    private Log log = LogFactory.get();

    @Override
    public void onEvent(HttpExchangeEvent httpExchangeWrapper, long l, boolean b) throws Exception {

    }

}
