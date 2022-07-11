package net.joojn.util;

import java.util.Objects;

public class Pair<K, V> {

    private final K key;
    private final V value;

    public Pair(K key, V value)
    {
        this.key = key;
        this.value = value;
    }
    @Override
    public String toString()
    {
        return this.key + " = " + this.value;
    }

    @Override
    public boolean equals(Object object)
    {
        if(object == this) return true;

        if(!(object instanceof Pair)) return false;

        Pair pair = (Pair) object;

        return Objects.equals(this.key, pair.key) && Objects.equals(this.value, pair.value);
    }

    public V getValue() {
        return this.value;
    }

    public K getKey()
    {
        return this.key;
    }
}
