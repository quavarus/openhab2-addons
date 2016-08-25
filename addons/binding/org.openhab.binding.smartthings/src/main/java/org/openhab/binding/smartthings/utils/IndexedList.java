package org.openhab.binding.smartthings.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * User: jhenry
 * Date: 3/31/2016
 * Time: 11:57 AM
 */
public class IndexedList<E> extends AbstractIndexedCollection<IndexedList<E>, E, List<E>> implements List<E> {

    public IndexedList(String defaultIndexName, ValueRetriever<E, ?>... valueRetrievers) {
        super(defaultIndexName, valueRetrievers);
    }

    public IndexedList(Collection<? extends E> items, ValueRetriever<E, ?>... valueRetrievers) {
        super(items, valueRetrievers);
    }

    public IndexedList(ValueRetriever<E, ?>... valueRetrievers) {
        super(valueRetrievers);
    }

    private IndexedList(IndexedList<E> sourceList) {
        super(sourceList);
    }

    @Override
    public IndexedList<E> copy() {
        return new IndexedList<>(this);
    }

    @Override
    public IndexedList<E> create(ValueRetriever<E, ? extends Object>... valueRetrievers) {
        return new IndexedList<>(valueRetrievers);
    }

    @Override
    protected List<E> createBackingCollection() {
        return new ArrayList<>();
    }

    @Override
    public E get(int index) {
        return getBackingCollection().get(index);
    }

    @Override
    public E set(int index, E element) {
        E oldElement = getBackingCollection().set(index, element);
        removeFromIndexes(oldElement);
        addToIndexes(element);
        return oldElement;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        int currentSize = this.size();
        for (E e : c) {
            this.add(index, e);
            index++;
        }
        return currentSize == this.size();
    }

    @Override
    public void add(int index, E element) {
        getBackingCollection().add(index, element);
        addToIndexes(element);
        modCount++;
    }

    @Override
    public E remove(int index) {
        E element = getBackingCollection().remove(index);
        removeFromIndexes(element);
        modCount++;
        return element;
    }

    @Override
    public int indexOf(Object o) {
        return getBackingCollection().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getBackingCollection().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListItr(getBackingCollection().listIterator(0));
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new ListItr(getBackingCollection().listIterator(index));
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return getBackingCollection().subList(fromIndex, toIndex);
    }

    private class ListItr implements ListIterator<E> {
        private ListIterator<E> iterator;
        private E current;

        ListItr(ListIterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            current = iterator.next();
            return current;
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public E previous() {
            current = iterator.previous();
            return current;
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            iterator.remove();
            IndexedList.this.removeFromIndexes(current);
        }

        @Override
        public void set(E e) {
            IndexedList.this.removeFromIndexes(current);
            iterator.set(e);
            IndexedList.this.addToIndexes(e);
            current = e;
        }

        @Override
        public void add(E e) {
            iterator.add(e);
            IndexedList.this.addToIndexes(e);
        }
    }
}
