package org.openhab.binding.smartthings.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: jhenry
 * Date: 7/21/2015
 * Time: 12:36 PM
 */
public class CollectionIndex<L extends IndexedCollection<L, E>, E> implements Index<L, E> {

    private final Map<Object, L> index = new HashMap<>();
    private final List<ValueRetriever<E, ? extends Object>> valueGenerators;
    private final IndexedCollection<L, E> indexedCollection;

    CollectionIndex(IndexedCollection<L, E> indexedCollection, ValueRetriever<E, ? extends Object>... valueGenerators) {
        List<ValueRetriever<E, ? extends Object>> indexRetrievers = new ArrayList<ValueRetriever<E, ? extends Object>>(
                Arrays.asList(valueGenerators));
        indexRetrievers.removeAll(Arrays.asList(new ValueRetriever[] { null }));
        this.valueGenerators = indexRetrievers;
        this.indexedCollection = indexedCollection;
    }

    CollectionIndex(IndexedCollection<L, E> indexedCollection,
            List<ValueRetriever<E, ? extends Object>> valueGenerators) {
        this.valueGenerators = valueGenerators;
        this.indexedCollection = indexedCollection;
    }

    @Override
    public boolean containsKey(Object... keys) {
        return containsKey(new LinkedList<>(Arrays.asList(keys)));
    }

    @Override
    public boolean containsKey(List<Object> keys) {
        L returnValue = find(keys);
        return returnValue.size() > 0;
    }

    @Override
    public E findFirst(Object... keys) {
        L list = find(new LinkedList<Object>(Arrays.asList(keys)));
        if (list.size() > 0) {
            return list.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public E findOne(Object... keys) {
        L list = find(new LinkedList<Object>(Arrays.asList(keys)));
        if (list.size() > 1) {
            throw new ValueNotUniqueException();
        } else if (list.size() == 0) {
            throw new ValueNotFoundException();
        } else {
            return list.iterator().next();
        }
    }

    @Override
    public L find(Object... keys) {
        return find(new LinkedList<Object>(Arrays.asList(keys)));
    }

    @Override
    public L find(List<Object> keys) {
        L returnValue = index.get(keys.get(0));
        if (keys.size() == 1 || returnValue == null) {
            return returnValue == null ? indexedCollection.create() : returnValue.copy();
        } else {
            keys.remove(0);
            return returnValue.index().find(keys);
        }
    }

    @Override
    public Set<Object> keys() {
        return index.keySet();
    }

    @Override
    public <E> Set<E> keys(Class<E> clazz) {
        return castSet(clazz, index.keySet());
    }

    private <E> Set<E> castSet(Class<E> clazz, Collection collection) {
        Set<E> returns = new HashSet<>();
        for (Object item : collection) {
            returns.add((E) item);
        }
        return returns;
    }

    @Override
    public void add(E item) {
        if (valueGenerators.size() > 0) {
            Object key = valueGenerators.get(0).get(item);
            if (!index.containsKey(key)) {
                if (valueGenerators.size() > 1) {
                    index.put(key, indexedCollection.create(valueGenerators.subList(1, valueGenerators.size())
                            .toArray(new ValueRetriever[valueGenerators.size() - 1])));
                } else {
                    index.put(key, indexedCollection.create());
                }
            }
            index.get(key).add(item);
        }
    }

    @Override
    public void remove(E item) {
        if (valueGenerators.size() > 0) {
            Object key = valueGenerators.get(0).get(item);
            index.get(key).remove(item);
            if (index.get(key).size() == 0) {
                index.remove(key);
            }
        }
    }

    @Override
    public void clear() {
        index.clear();
    }
}
