package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.serializers.IntegerSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class IntegerMapBenchmark {

    @Param({"100", "10000"})
    private int size;

    private Map<Integer, Integer> hashMap;
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

        hashMap = new HashMap<>(size);
        offHeapMap = new OffHeapMap<>(new IntegerSerializer(), new IntegerSerializer(), size);

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
        Map<Integer, Integer> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
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
    public Integer javaHashMap_get() {
        Integer result = null;
        for (int i = 0; i < size; i++) {
            result = hashMap.get(keys[i]);
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
