package org.openhab.binding.smartthings.utils;

import java.util.List;
import java.util.Set;

/**
 * User: jhenry
 * Date: 3/31/2016
 * Time: 8:13 AM
 */
public interface Index<L, E> {
    /**
     * Returns true/false if a value is or is not found in the index for the supplied keys
     * 
     * @param keys
     * @return
     */
    boolean containsKey(Object... keys);

    /**
     * Returns true/false if a value is or is not found in the index for the supplied keys
     * 
     * @param keys
     * @return
     */
    boolean containsKey(List<Object> keys);

    /**
     * Finds the first value in the index matching the supplied keys. <br/>
     * Returns null if nothing is found.
     * 
     * @param keys the keys search the index with
     * @return the first value found or null if not found
     */
    E findFirst(Object... keys);

    /**
     * Finds one value in the index matching the supplied keys. <br/>
     * If the index does not contain a value or the value is not unique and exception is thrown.
     * 
     * @param keys the keys to search the index with
     * @return the one value matching the keys
     * @throws ValueNotFoundException when no matching value is found in the index.
     * @throws ValueNotUniqueException when more than one matching value is found in the index.
     */
    E findOne(Object... keys);

    /**
     * Finds a list of values in the index matching the supplied keys. <br/>
     * If the index does not contain a matching value an empty list is returned.
     * 
     * @param keys the keys to search the index with
     * @return a list of matching values or an empty list if there is no match
     */
    L find(Object... keys);

    /**
     * Finds a list of values in the index matching the supplied keys. <br/>
     * If the index does not contain a matching value an empty list is returned.
     * 
     * @param keys the keys to search the index with
     * @return a list of matching values or an empty list if there is no match
     */
    L find(List<Object> keys);

    /**
     * Gets the set of keys at this level of the index.
     * 
     * @return
     */
    Set<Object> keys();

    /**
     * Gets the set of keys at this level of the index. <br/>
     * Attempts to cast the set to a known type. Will throw a ClassCastException if it can't.
     * 
     * @param clazz the class to attempt to cast the set items into.
     * @return
     */
    <E> Set<E> keys(Class<E> clazz);

    /**
     * Adds the specified item to the index.
     * 
     * @param item
     */
    void add(E item);

    /**
     * Removes the specified item from the index.
     * 
     * @param item
     */
    void remove(E item);

    /**
     * removes all items from the index.
     */
    void clear();
}
