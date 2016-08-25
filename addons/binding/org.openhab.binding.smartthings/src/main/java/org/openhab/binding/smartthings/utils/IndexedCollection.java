package org.openhab.binding.smartthings.utils;

import java.util.Collection;

/**
 * User: jhenry
 * Date: 3/31/2016
 * Time: 9:54 AM
 */
public interface IndexedCollection<L, E> extends Collection<E> {
    void addIndex(String name, ValueRetriever<E, ? extends Object>... valueRetrievers);

    void removeIndex(String name);

    Index<L, E> index(String name);

    Index<L, E> index();

    L copy();

    L create(ValueRetriever<E, ? extends Object>... valueRetrievers);

}
