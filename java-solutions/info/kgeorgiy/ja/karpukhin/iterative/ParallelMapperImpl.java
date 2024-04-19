package info.kgeorgiy.ja.karpukhin.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads = new ArrayList<>();
    private final Queue<Runnable> tasks = new LinkedList<>();

    public ParallelMapperImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                tasks.wait();
                            }
                            task = tasks.poll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                }
            });
            thread.start();
            this.threads.add(thread);
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        final Counter counter = new Counter(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            submit(() -> result.set(index, f.apply(args.get(index))), counter);
        }

        synchronized (counter) {
            while (counter.getValue() > 0) {
                counter.wait();
            }
        }
        return result;
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void submit(Runnable task, Counter counter) {
        synchronized (tasks) {
            tasks.add(() -> {
                task.run();
                synchronized (counter) {
                    counter.decrement();
                    counter.notify();
                }
            });
            tasks.notify();
        }
    }

    static class Counter {
        private int value;

        public Counter(int value) {
            this.value = value;
        }

        public void increment() {
            value++;
        }

        public void decrement() {
            value--;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}