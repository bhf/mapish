package com.bhf.mapish;

import com.bhf.mapish.serializers.*;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class OffHeapStringMapTest {

    public static Test suite() {
        return MapTestSuiteBuilder
                .using(new TestMapGenerator<String, String>() {
                    @Override
                    public String[] createKeyArray(int length) {
                        return new String[length];
                    }

                    @Override
                    public String[] createValueArray(int length) {
                        return new String[length];
                    }

                    @Override
                    public SampleElements<Map.Entry<String, String>> samples() {
                        return new SampleElements<>(
                            new AbstractMap.SimpleEntry<>("key1", "val1"),
                            new AbstractMap.SimpleEntry<>("key2", "val2"),
                            new AbstractMap.SimpleEntry<>("key3", "val3"),
                            new AbstractMap.SimpleEntry<>("key4", "val4"),
                            new AbstractMap.SimpleEntry<>("key5", "val5")
                        );
                    }

                    @Override
                    public Map<String, String> create(Object... elements) {
                        // 128 max bytes for string length
                        OffHeapMap<String, String> map = new OffHeapMap<>(new StringSerializer(128), new StringSerializer(128));
                        for (Object o : elements) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
                            map.put(entry.getKey(), entry.getValue());
                        }
                        return map;
                    }

                    @Override
                    public Map.Entry<String, String>[] createArray(int length) {
                        return new Map.Entry[length];
                    }

                    @Override
                    public Iterable<Map.Entry<String, String>> order(List<Map.Entry<String, String>> insertionOrder) {
                        return insertionOrder;
                    }
                })
                .named("OffHeapMap String Tests")
                .withFeatures(
                        MapFeature.GENERAL_PURPOSE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionSize.ANY
                )
                .createTestSuite();
    }
}
