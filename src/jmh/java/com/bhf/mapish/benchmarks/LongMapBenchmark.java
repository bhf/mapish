package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.LongSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class LongMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<Long, Long> oakMap;
    private OffHeapMap<Long, Long> offHeapMap;

    private Long[] keys;
    private Long[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Long[size];
        values = new Long[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (long) i;
            values[i] = (long) i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.LONG_COMPARATOR, OakAdapters.LONG_SERIALIZER, OakAdapters.LONG_SERIALIZER, Long.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new LongSerializer(), new LongSerializer(), size);

        for (int i = 0; i < size; i++) {
            oakMap.put(keys[i], values[i]);
            offHeapMap.put(keys[i], values[i]);
        }
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        if (oakMap != null) {
            oakMap.close();
        }
    }

    @Benchmark
    public void oakMap_put() {
        try (OakMap<Long, Long> oak = new OakMapBuilder<>(OakAdapters.LONG_COMPARATOR, OakAdapters.LONG_SERIALIZER, OakAdapters.LONG_SERIALIZER, Long.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Long, Long> map = new OffHeapMap<>(new LongSerializer(), new LongSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Long oakMap_get() {
        Long result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Long offHeapMap_get() {
        Long result = null;
        for (int i = 0; i < size; i++) {
            result = offHeapMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public boolean oakMap_containsKey() {
        boolean result = false;
        for (int i = 0; i < size; i++) {
            result = oakMap.containsKey(keys[i]);
        }
        return result;
    }

    @Benchmark
    public boolean offHeapMap_containsKey() {
        boolean result = false;
        for (int i = 0; i < size; i++) {
            result = offHeapMap.containsKey(keys[i]);
        }
        return result;
    }
}
