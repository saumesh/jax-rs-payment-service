package com.saumesh.payment.persistenance;

public interface KeyValueStore<K, V> {
    V get(K key);
    boolean add(K key, V value);
    V update(K key, V value);
    V delete(K key);
}
