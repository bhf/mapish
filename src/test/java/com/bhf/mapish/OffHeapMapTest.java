package com.bhf.mapish;

import com.bhf.mapish.serializers.*;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class OffHeapMapTest {

    public static Test suite() {
        return MapTestSuiteBuilder
                .using(new TestMapGenerator<Long, Long>() {
                    @Override
                    public Long[] createKeyArray(int length) {
                        return new Long[length];
                    }

                    @Override
                    public Long[] createValueArray(int length) {
                        return new Long[length];
                    }

                    @Override
                    public SampleElements<Map.Entry<Long, Long>> samples() {
                        return new SampleElements<>(
                            new AbstractMap.SimpleEntry<>(1L, 100L),
                            new AbstractMap.SimpleEntry<>(2L, 200L),
                            new AbstractMap.SimpleEntry<>(3L, 300L),
                            new AbstractMap.SimpleEntry<>(4L, 400L),
                            new AbstractMap.SimpleEntry<>(5L, 500L)
                        );
                    }

                    @Override
                    public Map<Long, Long> create(Object... elements) {
                        OffHeapMap<Long, Long> map = new OffHeapMap<>(new LongSerializer(), new LongSerializer());
                        for (Object o : elements) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<Long, Long> entry = (Map.Entry<Long, Long>) o;
                            map.put(entry.getKey(), entry.getValue());
                        }
                        return map;
                    }

                    @Override
                    public Map.Entry<Long, Long>[] createArray(int length) {
                        return new Map.Entry[length];
                    }

                    @Override
                    public Iterable<Map.Entry<Long, Long>> order(List<Map.Entry<Long, Long>> insertionOrder) {
                        return insertionOrder;
                    }
                })
                .named("OffHeapMap Tests")
                .withFeatures(
                        MapFeature.GENERAL_PURPOSE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionSize.ANY
                )
                .createTestSuite();
    }
}
