package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** 
 * Locking manager that provides distinct locks based on a key value.
 * 
 * <p>Unlike a normal lock, the locks provided by this type are not bound to a particular
 * thread, and so any thread with a reference to the returned lock may release it.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 * @apiNote The goal of this type is to provide a relatively low-overhead protection mechanism
 *          for regions that should be mutually exclusive based on some parameter (rather than
 *          globaly), and the value space of that parameter is large and/or not known ahead
 *          of time, making a simple map of normal locks impractical or inneficient.
 */
public interface LockMap<K extends @NonNull Object> {

    /**
     * Acquires the lock of the given key, if currently available.
     *
     * @param key The key to acquire a lock for.
     * @return The acquired lock, or {@code null} if the lock is in use and could not be acquired.
     */
    @Nullable AcquiredLock tryAcquire( K key );

    /**
     * Retrieves a lock in this map.
     *
     * @param key The lock's key.
     * @return The lock with the given key.
     * @apiNote Using the returned lock is functionally the same as directly calling the
     *          correspoding methods in the map with the given key. This is mostly a convenience 
     *          for re-acquiring a lock and for compatibility with APIs that need a lock.
     * @implSpec Implementations should have very little overhead (in terms of both runtime and 
     *           memory usage) compared to using the map directly.
     */
    Lock get( K key );
    
}
