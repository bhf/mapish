package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.serializers.FloatSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class FloatMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private Map<Float, Float> hashMap;
    private OffHeapMap<Float, Float> offHeapMap;

    private Float[] keys;
    private Float[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Float[size];
        values = new Float[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (float) i;
            values[i] = (float) i;
        }

        hashMap = new HashMap<>(size);
        offHeapMap = new OffHeapMap<>(new FloatSerializer(), new FloatSerializer(), size);

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
        Map<Float, Float> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Float, Float> map = new OffHeapMap<>(new FloatSerializer(), new FloatSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Float javaHashMap_get() {
        Float result = null;
        for (int i = 0; i < size; i++) {
            result = hashMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Float offHeapMap_get() {
        Float result = null;
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
