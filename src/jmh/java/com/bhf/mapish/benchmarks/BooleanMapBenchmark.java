package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.BooleanSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BooleanMapBenchmark {

    @Param({"2"})
    private int size;

    private OakMap<Boolean, Boolean> oakMap;
    private OffHeapMap<Boolean, Boolean> offHeapMap;

    private Boolean[] keys;
    private Boolean[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Boolean[size];
        values = new Boolean[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (i % 2 == 0);
            values[i] = (i % 2 == 0);
        }

        oakMap = new OakMapBuilder<>(OakAdapters.BOOLEAN_COMPARATOR, OakAdapters.BOOLEAN_SERIALIZER, OakAdapters.BOOLEAN_SERIALIZER, false)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new BooleanSerializer(), new BooleanSerializer(), size);

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
        try (OakMap<Boolean, Boolean> oak = new OakMapBuilder<>(OakAdapters.BOOLEAN_COMPARATOR, OakAdapters.BOOLEAN_SERIALIZER, OakAdapters.BOOLEAN_SERIALIZER, false)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Boolean, Boolean> map = new OffHeapMap<>(new BooleanSerializer(), new BooleanSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Boolean oakMap_get() {
        Boolean result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Boolean offHeapMap_get() {
        Boolean result = null;
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
