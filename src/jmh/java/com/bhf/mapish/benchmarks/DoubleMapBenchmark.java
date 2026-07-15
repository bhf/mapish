package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.DoubleSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class DoubleMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<Double, Double> oakMap;
    private OffHeapMap<Double, Double> offHeapMap;

    private Double[] keys;
    private Double[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Double[size];
        values = new Double[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (double) i;
            values[i] = (double) i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.DOUBLE_COMPARATOR, OakAdapters.DOUBLE_SERIALIZER, OakAdapters.DOUBLE_SERIALIZER, Double.NEGATIVE_INFINITY)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new DoubleSerializer(), new DoubleSerializer(), size);

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
        try (OakMap<Double, Double> oak = new OakMapBuilder<>(OakAdapters.DOUBLE_COMPARATOR, OakAdapters.DOUBLE_SERIALIZER, OakAdapters.DOUBLE_SERIALIZER, Double.NEGATIVE_INFINITY)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Double, Double> map = new OffHeapMap<>(new DoubleSerializer(), new DoubleSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Double oakMap_get() {
        Double result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Double offHeapMap_get() {
        Double result = null;
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
