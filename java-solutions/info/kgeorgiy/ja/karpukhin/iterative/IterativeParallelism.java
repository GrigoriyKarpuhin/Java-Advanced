package info.kgeorgiy.ja.karpukhin.iterative;

import info.kgeorgiy.java.advanced.iterative.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {

    private <T> List<List<? extends T>> split(int threads, List<? extends T> values) {
        List<List<? extends T>> result = new ArrayList<>();
        int size = values.size() / threads;
        int mod = values.size() % threads;
        int left = 0;
        for (int i = 0; i < threads; i++) {
            int right = left + size;
            if (i < mod) right++;
            result.add(values.subList(left, right));
            left = right;
        }
        return result;
    }

    private <T, R> List<R> executeInThreads(int threads, List<? extends T> values,
                                            Function<List<? extends T>, R> function) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        List<R> result = new ArrayList<>();
        List<List<? extends T>> parts = split(threads, values);
        for (List<? extends T> part : parts) {
            Thread thread = new Thread(() -> {
                R res = function.apply(part);
                if (res != null) {
                    synchronized (result) {
                        result.add(res);
                    }
                }
            });
            thread.start();
            threadList.add(thread);
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        return result;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        List<T> maxValues = executeInThreads(threads, values, part -> part.stream().max(comparator).orElse(null));
        return maxValues.stream().max(comparator).orElse(null);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return count(threads, values, predicate) == values.size();
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return count(threads, values, predicate) > 0;
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        List<Integer> counts = executeInThreads(threads, values, part -> (int) part.stream().filter(predicate).count());
        return counts.stream().mapToInt(Integer::intValue).sum();
    }
}