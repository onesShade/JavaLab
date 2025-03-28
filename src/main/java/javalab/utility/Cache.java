package javalab.utility;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Cache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public Cache(int maxSize) {
        super(maxSize + 2, 1.f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cache<?, ?> that = (Cache<?, ?>) o;
        return maxSize == that.maxSize && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxSize, super.hashCode());
    }
}