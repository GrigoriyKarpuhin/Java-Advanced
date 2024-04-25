package info.kgeorgiy.ja.karpukhin.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads = new ArrayList<>();
    private final Queue<Runnable> tasks = new LinkedList<>();

    public ParallelMapperImpl(int threads) {
        Runnable taskRunner = () -> {
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
        };

        IntStream.iterate(0, i -> i < threads, i -> i + 1)
                .forEach(i -> {
                    Thread thread = new Thread(taskRunner);
                    thread.start();
                    this.threads.add(thread);
                });
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        final Counter counter = new Counter(args.size());
        IntStream.iterate(0, i -> i < args.size(), i -> i + 1)
                .forEach(i -> {
                    final int index = i;
                    submit(() -> result.set(index, f.apply(args.get(index))), counter);
                });

        while (counter.getValue() > 0) {
            counter.wait();
        }
        return result;
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

    private static class Counter {
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

        public synchronized int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}