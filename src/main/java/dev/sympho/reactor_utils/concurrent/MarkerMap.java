package dev.sympho.reactor_utils.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.checkerframework.checker.interning.qual.UsesObjectEquals;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Specialized map for storing unique markers in a concurrent environment.
 * All methods of this class are thread-safe.
 * 
 * <p>Marker instances are unique and one-off; that is, once a marker is removed from this map
 * (including if it is replaced), it cannot ever be inserted again.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 */
public final class MarkerMap<K extends @NonNull Object> {

    /** The backing map. */
    private final ConcurrentMap<K, Marker> markers = new ConcurrentHashMap<>();

    /** Creates a new instance. */
    public MarkerMap() {}

    /**
     * Places a new marker in the map. Any existing marker is replaced.
     *
     * @param key The key to place the marker under.
     * @return The marker.
     */
    public Marker place( final K key ) {

        final var marker = new Marker();
        markers.put( key, marker );
        return marker;

    }

    /**
     * Places a new marker in the map, if there is currently no marker on the given key.
     *
     * @param key The key to place the marker under.
     * @return The marker, or {@code null} if there is already a marker under the given key.
     */
    public @Nullable Marker placeIfAbsent( final K key ) {

        final var marker = new Marker();
        return markers.putIfAbsent( key, marker ) == null ? marker : null;

    }

    /**
     * Retrieves the marker under the given key, if any.
     *
     * @param key The key to get the current marker for.
     * @return The marker, or {@code null} if there no marker under the given key.
     */
    public @Nullable Marker get( final K key ) {

        return markers.get( key );

    }

    /**
     * Removes the marker under the given key, if any.
     *
     * @param key The key to remove the current marker for.
     * @return The removed marker, or {@code null} if there no marker under the given key.
     */
    public @Nullable Marker remove( final K key ) {

        return markers.remove( key );

    }

    /**
     * Removes the marker under the given key, if it matches the given marker.
     *
     * @param key The key to remove the current marker for.
     * @param marker The marker to check for.
     * @return {@code true} if the marker was removed.
     */
    public boolean remove( final K key, final Marker marker ) {

        return markers.remove( key, marker );

    }

    /**
     * A marker in the map. Instances may be compared for equality using {@code ==}.
     *
     * @since 1.0
     */
    @UsesObjectEquals
    public static final class Marker {

        /** Creates a new instance. */
        private Marker() {}

    }
    
}
