package com.bhf.mapish.benchmarks;

import com.bhf.mapish.OffHeapMap;
import com.bhf.mapish.benchmarks.oak.OakAdapters;
import com.bhf.mapish.serializers.CharacterSerializer;
import com.yahoo.oak.OakMap;
import com.yahoo.oak.OakMapBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class CharacterMapBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    private int size;

    private OakMap<Character, Character> oakMap;
    private OffHeapMap<Character, Character> offHeapMap;

    private Character[] keys;
    private Character[] values;

    @Setup(Level.Iteration)
    public void setup() {
        keys = new Character[size];
        values = new Character[size];
        for (int i = 0; i < size; i++) {
            keys[i] = (char) i;
            values[i] = (char) i;
        }

        oakMap = new OakMapBuilder<>(OakAdapters.CHARACTER_COMPARATOR, OakAdapters.CHARACTER_SERIALIZER, OakAdapters.CHARACTER_SERIALIZER, Character.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap();
        offHeapMap = new OffHeapMap<>(new CharacterSerializer(), new CharacterSerializer(), size);

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
        try (OakMap<Character, Character> oak = new OakMapBuilder<>(OakAdapters.CHARACTER_COMPARATOR, OakAdapters.CHARACTER_SERIALIZER, OakAdapters.CHARACTER_SERIALIZER, Character.MIN_VALUE)
                .setMemoryCapacity(OakAdapters.oakCapacity(size))
                .buildOrderedMap()) {
            for (int i = 0; i < size; i++) {
                oak.put(keys[i], values[i]);
            }
        }
    }

    @Benchmark
    public void offHeapMap_put() {
        OffHeapMap<Character, Character> map = new OffHeapMap<>(new CharacterSerializer(), new CharacterSerializer(), size);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }
    }

    @Benchmark
    public Character oakMap_get() {
        Character result = null;
        for (int i = 0; i < size; i++) {
            result = oakMap.get(keys[i]);
        }
        return result;
    }

    @Benchmark
    public Character offHeapMap_get() {
        Character result = null;
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
