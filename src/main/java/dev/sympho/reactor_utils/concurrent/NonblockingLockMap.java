package dev.sympho.reactor_utils.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** 
 * Lock map that is purely non-blocking; an attempt to acquire a lock will always fail immediately
 * if the lock is currently in use.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 * @apiNote This is a minimal-overhead implementation to optimize use cases in which waiting for
 *          a lock is never necessary.
 */
public final class NonblockingLockMap<K extends @NonNull Object> extends AbstractLockMap<K> {

    /** Marks a lock as taken. */
    private static final Object LOCK_OBJ = new Object();

    /** Pseudo-lock map. */
    private final ConcurrentMap<K, Object> locks;

    /**
     * Creates a new instance.
     */
    public NonblockingLockMap() {

        locks = new ConcurrentHashMap<>();

    }

    @Override
    public @Nullable AcquiredLock tryAcquire( final K key ) {

        if ( locks.put( key, LOCK_OBJ ) == null ) {
            return new MapAcquiredLock( key );
        } else {
            return null;
        }

    }

    /**
     * The lock implementation.
     *
     * @since 1.0
     */
    private final class MapAcquiredLock extends AbstractMapAcquiredLock {

        /**
         * Initializes a lock.
         *
         * @param key The lock's key.
         */
        private MapAcquiredLock( final K key ) {

            super( key );

        }

        @Override
        public void doRelease() {

            locks.remove( key );

        }

    } 
    
}
