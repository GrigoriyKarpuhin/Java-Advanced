package info.kgeorgiy.ja.karpukhin.iterative;

import info.kgeorgiy.java.advanced.iterative.NewScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class IterativeParallelism implements NewScalarIP {
    private final ParallelMapper parallelMapper;

    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T> List<List<? extends T>> split(int threads, List<? extends T> values, int step) {
        List<List<? extends T>> result = new ArrayList<>();
        List<? extends T> filteredValues = IntStream.iterate(0, i -> i < values.size(), i -> i + step)
                .mapToObj(values::get).toList();
        int size = filteredValues.size() / threads;
        int mod = filteredValues.size() % threads;
        int left = 0;
        for (int i = 0; i < threads; i++) {
            int right = left + size + (mod > 0 ? 1 : 0);
            mod--;
            result.add(filteredValues.subList(left, right));
            left = right;
        }
        return result;
    }

    private <T, R> List<R> executeInThreads(int threads, List<? extends T> values,
                                            Function<List<? extends T>, R> function, int step) throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Threads number should be positive");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values list should not be empty");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("Step should be positive");
        }
        int myThreads = Math.min(threads, (values.size() + step - 1) / step);
        if (parallelMapper == null) {
            List<Thread> threadList = new ArrayList<>();
            List<R> result = new ArrayList<>(Collections.nCopies(myThreads, null));
            List<List<? extends T>> parts = split(myThreads, values, step);
            for (int i = 0; i < myThreads; i++) {
                int index = i;
                threadList.add(new Thread(() -> result.set(index, function.apply(parts.get(index)))));
                threadList.get(i).start();
            }
            for (Thread thread : threadList) {
                thread.join();
            }
            return result;
        } else {
            return parallelMapper.map(function, split(myThreads, values, step));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step) throws InterruptedException {
        return executeInThreads(threads, values, part -> part.stream().max(comparator).orElse(null), step)
                .stream().max(comparator).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step) throws InterruptedException {
        return maximum(threads, values, comparator.reversed(), step);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        return executeInThreads(threads, values, part -> part.stream().allMatch(predicate), step)
                .stream().allMatch(Boolean::booleanValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        return !all(threads, values, predicate.negate(), step);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate, int step) throws InterruptedException {
        return executeInThreads(threads, values, part -> (int) part.stream().filter(predicate).count(), step)
                .stream().mapToInt(Integer::intValue).sum();
    }
}