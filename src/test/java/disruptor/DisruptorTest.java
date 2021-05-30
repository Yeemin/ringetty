package disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DisruptorTest {

    class DemoEvent {
        String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    class DemoEventFactory implements EventFactory<DemoEvent> {

        @Override
        public DemoEvent newInstance() {
            return new DemoEvent();
        }
    }


    class DemoEventHandler implements EventHandler<DemoEvent> {

        @Override
        public void onEvent(DemoEvent demoEvent, long l, boolean b) throws Exception {
            System.out.println(Thread.currentThread().getName() + ": " + demoEvent.getValue());
        }
    }

    class DemoEventProducer {
        private final RingBuffer<DemoEvent> ringBuffer;

        DemoEventProducer(RingBuffer<DemoEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        public void onData(String data) {
            long sequence = ringBuffer.next();
            try {
                DemoEvent demoEvent = ringBuffer.get(sequence);
                demoEvent.setValue(data);
            } finally {
                ringBuffer.publish(sequence);
            }
        }
    }

    class DemoEventProducerWithTranslator {
        private final EventTranslatorOneArg<DemoEvent, String> TRANSLATOR = new EventTranslatorOneArg<DemoEvent, String>() {
            @Override
            public void translateTo(DemoEvent demoEvent, long l, String s) {
                demoEvent.setValue(s);
            }
        };

        private final RingBuffer<DemoEvent> ringBuffer;


        DemoEventProducerWithTranslator(RingBuffer<DemoEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        public void onData(String data) {

            ringBuffer.publishEvent(TRANSLATOR, data);
        }
    }

    @Test
    public void test() {
        AtomicInteger index = new AtomicInteger();
        ThreadFactory threadFactory = r -> new Thread(r, "demo-" + index.incrementAndGet());
        DemoEventFactory demoEventFactory = new DemoEventFactory();
        int bufferSize = 1024;
        Disruptor<DemoEvent> disruptor = new Disruptor<>(demoEventFactory, bufferSize, threadFactory);
        disruptor.handleEventsWith(new DemoEventHandler());
        disruptor.start();

        RingBuffer<DemoEvent> ringBuffer = disruptor.getRingBuffer();

        DemoEventProducer demoEventProducer = new DemoEventProducer(ringBuffer);
        for (int i = 0; i < 10000000; i++) {
            demoEventProducer.onData("data - " + (i + 1));
        }


        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lambda1() {
        Disruptor<DemoEvent> disruptor = new Disruptor<>(DemoEvent::new, 1024, (ThreadFactory) Thread::new);
        disruptor.handleEventsWith((demoEvent, l, b) -> System.out.println(demoEvent.getValue()));
        disruptor.start();

        RingBuffer<DemoEvent> ringBuffer = disruptor.getRingBuffer();
        IntStream.range(0, 100000).boxed().forEach(integer ->
                ringBuffer.publishEvent((event, l) -> event.setValue("data-" + integer))
        );
    }

    static class DemoEventWithMethodRef {
        public static void handleEvent(DemoEvent event, long sequence, boolean endOfBatch) {
            System.out.println(event.getValue());
        }

        public static void translate(DemoEvent event, long sequence, String data) {
            event.setValue(data);
        }
    }

    @Test
    public void lambda2() {
        Disruptor<DemoEvent> disruptor = new Disruptor<>(DemoEvent::new, 1024, (ThreadFactory) Thread::new);
        disruptor.handleEventsWith(DemoEventWithMethodRef::handleEvent);
        disruptor.start();

        RingBuffer<DemoEvent> ringBuffer = disruptor.getRingBuffer();
        IntStream.range(0, 100000).boxed().forEach(integer ->
                ringBuffer.publishEvent(DemoEventWithMethodRef::translate, "data-" + integer)
        );
    }


}
