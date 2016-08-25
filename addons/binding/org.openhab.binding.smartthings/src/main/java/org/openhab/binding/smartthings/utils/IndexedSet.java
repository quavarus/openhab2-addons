package org.openhab.binding.smartthings.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: jhenry
 * Date: 3/31/2016
 * Time: 11:28 AM
 */
public class IndexedSet<E> extends AbstractIndexedCollection<IndexedSet<E>, E, Set<E>> implements Set<E> {

    public IndexedSet(String defaultIndexName, ValueRetriever<E, ?>... valueRetrievers) {
        super(defaultIndexName, valueRetrievers);
    }

    public IndexedSet(Collection<? extends E> items, ValueRetriever<E, ?>... valueRetrievers) {
        super(items, valueRetrievers);
    }

    public IndexedSet(ValueRetriever<E, ?>... valueRetrievers) {
        super(valueRetrievers);
    }

    private IndexedSet(IndexedSet<E> sourceList) {
        super(sourceList);
    }

    @Override
    public IndexedSet<E> copy() {
        return new IndexedSet<>(this);
    }

    @Override
    public IndexedSet<E> create(ValueRetriever<E, ? extends Object>... valueRetrievers) {
        return new IndexedSet<>(valueRetrievers);
    }

    @Override
    protected Set<E> createBackingCollection() {
        return new HashSet<>();
    }
}
