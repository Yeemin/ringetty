package org.example.ringetty.handler;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.lmax.disruptor.EventHandler;
import org.example.ringetty.dsl.DisruptorManager;
import org.example.ringetty.event.HttpExchangeEvent;

public class DispatcherHandler implements EventHandler<HttpExchangeEvent> {

    private Log log = LogFactory.get();

    public DispatcherHandler() {
        DisruptorManager.dispatchHandleEventWith(this);
    }

    @Override
    public void onEvent(HttpExchangeEvent httpExchangeEvent, long l, boolean b) throws Exception {
        log.info("dispatch http");
        DisruptorManager.send(httpExchangeEvent);
    }
}
