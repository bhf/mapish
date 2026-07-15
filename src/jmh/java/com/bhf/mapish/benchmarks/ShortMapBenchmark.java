package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.ShortSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ShortMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<Short, Short> oakMap;
    private OffHeapMap<Short, Short> offHeapMap;

    private Short[] keys;
    private Short[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Short[size];
        values = new Short[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (short) i;
            values[i] = (short) i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.SHORT_COMPARATOR, OakAdapters.SHORT_SERIALIZER, OakAdapters.SHORT_SERIALIZER, Short.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new ShortSerializer(), new ShortSerializer(), size);

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
        try (OakMap<Short, Short> oak = new OakMapBuilder<>(OakAdapters.SHORT_COMPARATOR, OakAdapters.SHORT_SERIALIZER, OakAdapters.SHORT_SERIALIZER, Short.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Short, Short> map = new OffHeapMap<>(new ShortSerializer(), new ShortSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Short oakMap_get() {
        Short result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Short offHeapMap_get() {
        Short result = null;
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
