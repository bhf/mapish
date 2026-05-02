package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.serializers.DoubleSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class DoubleMapBenchmark {

    @Param({"100", "10000"})
    private int size;

    private Map<Double, Double> hashMap;
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

        hashMap = new HashMap<>(size);
        offHeapMap = new OffHeapMap<>(new DoubleSerializer(), new DoubleSerializer(), size);

        for (int i = 0; i < size; i++) {
            hashMap.put(keys[i], values[i]);
            offHeapMap.put(keys[i], values[i]);
        }
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        // Arena.ofAuto() handles it
    }

    @Benchmark
    public void javaHashMap_put() {
        Map<Double, Double> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
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
    public Double javaHashMap_get() {
        Double result = null;
        for (int i = 0; i < size; i++) {
            result = hashMap.get(keys[i]);
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
    public boolean javaHashMap_containsKey() {
        boolean result = false;
        for (int i = 0; i < size; i++) {
            result = hashMap.containsKey(keys[i]);
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
