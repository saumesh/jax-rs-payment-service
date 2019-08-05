package com.saumesh.payment.persistenance.memory;

import com.saumesh.payment.persistenance.KeyValueStore;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryKeyValueStore<K, V> implements KeyValueStore<K, V> {
    private Map<K, V> cacheStore;

    public InMemoryKeyValueStore(){
        this.cacheStore = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K key) {
        return cacheStore.get(key);
    }

    @Override
    public boolean add(K key, V value) {
        return null != cacheStore.put(key, value);
    }

    @Override
    public V update(K key, V value) {
        return cacheStore.put(key, value);
    }

    @Override
    public V delete(K key) {
        return cacheStore.remove(key);
    }
}
