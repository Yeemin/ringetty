package disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DisruptorTest {

    class DemoEvent {
        String value;

        public DemoEvent() {
        }

        public DemoEvent(String value) {
            this.value = value;
        }

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

    @Test
    public void pollTest() throws Exception {
        int size = 100;
        int bufferSize = 1;
        while (bufferSize < size) {
            bufferSize <<= 1;
        }
        Disruptor<DemoEvent> disruptor = new Disruptor<>(DemoEvent::new, bufferSize, (ThreadFactory) Thread::new);
        disruptor.start();

        RingBuffer<DemoEvent> ringBuffer = disruptor.getRingBuffer();
        IntStream.range(1, size + 1).boxed().forEach(integer ->
                ringBuffer.publishEvent(DemoEventWithMethodRef::translate, "data-" + integer)
        );

        EventPoller<DemoEvent> newPoller = ringBuffer.newPoller();
        Sequence sequence = newPoller.getSequence();

        CountDownLatch countDownLatch = new CountDownLatch(size);
        long time = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    DemoEvent demoEvent = ringBuffer.get(sequence.incrementAndGet());
                    System.out.println(Thread.currentThread().getName() + ": " + demoEvent.getValue());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("elapse: " + (System.currentTimeMillis() - time));
        Thread.sleep(1000L);

        ConcurrentLinkedQueue<DemoEvent> queue = new ConcurrentLinkedQueue<>();
        IntStream.range(1, size + 1).boxed().forEach(integer ->
                queue.offer(new DemoEvent("data-" + integer))
        );

        CountDownLatch countDownLatch2 = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    DemoEvent demoEvent = queue.poll();
//                    System.out.println(Thread.currentThread().getName() + ": " + demoEvent.getValue());
                } finally {
                    countDownLatch2.countDown();
                }
            });
        }
        countDownLatch2.await();
        System.out.println("elapse: " + (System.currentTimeMillis() - time));
    }

    @Test
    public void diamondTest() {
        Disruptor<DemoEvent> disruptor = new Disruptor<>(DemoEvent::new, 1024, (ThreadFactory) Thread::new);

        disruptor.handleEventsWith((event, l, b) -> System.out.println("1"), (event, l1, l2) -> System.out.println("2"))
                .handleEventsWith((event, l, b) -> System.out.println("3"))
                .then((event, l, b) -> System.out.println("4"));
        RingBuffer<DemoEvent> ringBuffer = disruptor.getRingBuffer();
        disruptor.handleEventsWith(new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(),
                (event, l, b) -> System.out.println("processor")))
        .then((event, l, b) -> System.out.println("processor"));

        disruptor.start();
        disruptor.publishEvent(((event, sequence) -> event.setValue("demo")));
        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
