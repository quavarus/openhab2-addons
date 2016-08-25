package org.openhab.binding.smartthings.utils;

/**
 * Retrieves one object based on the value of the source object.
 *
 * User: jhenry
 * Date: 2/23/2015
 * Time: 10:25 AM
 */
public interface ValueRetriever<S, T> {

    T get(S source);
}
