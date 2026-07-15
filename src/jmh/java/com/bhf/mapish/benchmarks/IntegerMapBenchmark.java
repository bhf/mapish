package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.IntegerSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class IntegerMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<Integer, Integer> oakMap;
    private OffHeapMap<Integer, Integer> offHeapMap;

    private Integer[] keys;
    private Integer[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Integer[size];
        values = new Integer[size];
        for (int i = 0; i < size; i++) {
            keys[i] = i;
            values[i] = i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.INTEGER_COMPARATOR, OakAdapters.INTEGER_SERIALIZER, OakAdapters.INTEGER_SERIALIZER, Integer.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new IntegerSerializer(), new IntegerSerializer(), size);

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
        try (OakMap<Integer, Integer> oak = new OakMapBuilder<>(OakAdapters.INTEGER_COMPARATOR, OakAdapters.INTEGER_SERIALIZER, OakAdapters.INTEGER_SERIALIZER, Integer.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Integer, Integer> map = new OffHeapMap<>(new IntegerSerializer(), new IntegerSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Integer oakMap_get() {
        Integer result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Integer offHeapMap_get() {
        Integer result = null;
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
