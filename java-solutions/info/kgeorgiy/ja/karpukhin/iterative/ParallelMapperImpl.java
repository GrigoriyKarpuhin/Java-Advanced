package info.kgeorgiy.ja.karpukhin.iterative;


import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads = new ArrayList<>();
    private final Queue<Runnable> tasks = new LinkedList<>();

    public ParallelMapperImpl(int threads) {
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        return List.of();
    }

    @Override
    public void close() {
    }
}
