package org.openhab.binding.smartthings.utils;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: jhenry
 * Date: 2/20/2015
 * Time: 11:06 AM
 */
public abstract class AbstractIndexedCollection<L extends AbstractIndexedCollection<L, E, B>, E, B extends Collection<E>>
        implements IndexedCollection<L, E> {

    public static final String DEFAULT_INDEX = "$default";
    // private final HashSet<E> list;
    protected Map<String, CollectionIndex<L, E>> indexes = new HashMap<>();
    protected final String defaultIndexName;
    private final B list;

    /**
     * Creates an indexed list of items with a single default index defined by the value retrievers.
     *
     * @param valueRetrievers an array of value retrievers. One per item in the index.
     */
    public AbstractIndexedCollection(ValueRetriever<E, ? extends Object>... valueRetrievers) {
        list = createBackingCollection();
        defaultIndexName = DEFAULT_INDEX;
        addIndex(defaultIndexName, valueRetrievers);
    }

    /**
     * Creates an indexed list of items with a single default index defined by the value retrievers.
     *
     * @param valueRetrievers an array of value retrievers. One per item in the index.
     */
    public AbstractIndexedCollection(String defaultIndexName, ValueRetriever<E, ? extends Object>... valueRetrievers) {
        list = createBackingCollection();
        this.defaultIndexName = defaultIndexName;
        addIndex(defaultIndexName, valueRetrievers);
    }

    /**
     * Creates an indexed list of items with a single default index defined by the value retrievers.
     *
     * @param items the source list of items to index
     * @param valueRetrievers an array of value retrievers. One per item in the index.
     */
    public AbstractIndexedCollection(Collection<? extends E> items,
            ValueRetriever<E, ? extends Object>... valueRetrievers) {
        list = createBackingCollection();
        defaultIndexName = DEFAULT_INDEX;
        addIndex(defaultIndexName, valueRetrievers);
        this.addAll(items);
    }

    protected AbstractIndexedCollection(L sourceList) {
        this.list = createBackingCollection();
        this.list.addAll(sourceList.getBackingCollection());
        this.defaultIndexName = sourceList.defaultIndexName;
        this.indexes = new HashMap<>(sourceList.indexes);
    }

    protected abstract B createBackingCollection();

    protected B getBackingCollection() {
        return list;
    }

    /**
     * Adds an additional named index to this list. Allowing the list to be indexed multiple ways.
     *
     * @param name unique name of the new index.
     * @param valueRetrievers an array of value retrievers. One per item in the index.
     * @throws RuntimeException if index name already exists.
     **/
    @Override
    public void addIndex(String name, ValueRetriever<E, ? extends Object>... valueRetrievers) {
        if (indexes.containsKey(name)) {
            throw new RuntimeException("Cannot add index. Index with name '" + name + "' already exists.");
        }
        indexes.put(name, new CollectionIndex<>(this, valueRetrievers));
        for (E e : list) {
            addToIndex(name, e);
        }
    }

    /**
     * removes the index and indexed data from the list.
     *
     * @param name of the index to remove
     * @throws RuntimeException if the named index doesn't exist.
     */
    @Override
    public void removeIndex(String name) {
        index(name);
        indexes.remove(name);
    }

    @Override
    public CollectionIndex<L, E> index(String name) {
        CollectionIndex<L, E> index = indexes.get(name);
        if (index == null) {
            throw new RuntimeException("Requested Index '" + name + "' does not exist");
        }
        return index;
    }

    @Override
    public CollectionIndex<L, E> index() {
        return index(DEFAULT_INDEX);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr(list.iterator());
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(E e) {
        boolean changed = list.add(e);
        if (changed) {
            addToIndexes(e);
            modCount++;
        }
        return changed;
    }

    protected void addToIndexes(E e) {
        for (String name : indexes.keySet()) {
            addToIndex(name, e);
        }
    }

    protected void addToIndex(String name, E e) {
        index(name).add(e);
    }

    @Override
    public boolean remove(Object o) {
        boolean changed = list.remove(o);
        if (changed) {
            removeFromIndexes((E) o);
            modCount++;
        }
        return changed;
    }

    protected void removeFromIndexes(E e) {
        for (CollectionIndex<L, E> index : indexes.values()) {
            index.remove(e);
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object e : c) {
            if (this.remove(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int currentSize = this.size();
        Iterator<E> it = this.iterator();
        while (it.hasNext()) {
            E e = it.next();
            if (!c.contains(e)) {
                it.remove();
            }
        }
        return currentSize == this.size();
    }

    @Override
    public void clear() {
        list.clear();
        clearIndexes();
        modCount++;
    }

    private void clearIndexes() {
        for (CollectionIndex<L, E> index : indexes.values()) {
            index.clear();
        }
    }

    protected transient int modCount = 0;

    protected class Itr implements Iterator<E> {
        private final Iterator<E> iterator;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have. If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        int expectedModCount = modCount;

        private E currentObject;

        Itr(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            checkForComodification();
            return iterator.hasNext();
        }

        @Override
        public E next() {
            checkForComodification();
            currentObject = iterator.next();
            return currentObject;
        }

        @Override
        public void remove() {
            checkForComodification();
            AbstractIndexedCollection.this.removeFromIndexes(currentObject);
            expectedModCount = modCount;
            iterator.remove();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
