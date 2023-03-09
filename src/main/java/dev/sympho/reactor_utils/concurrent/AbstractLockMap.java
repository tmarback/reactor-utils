package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base implementation for {@link LockMap lock maps}.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractLockMap<K extends @NonNull Object> implements LockMap<K> {

    @Override
    public Lock get( final K key ) {

        return new WrapperLock( key );

    }

    /**
     * Overlay wrapper for a lock backed by this map under a given key.
     *
     * @since 1.0
     */
    protected class WrapperLock implements Lock {

        /** The key of the lock. */
        protected final K key;

        /**
         * Creates a new instance.
         *
         * @param key The lock key.
         */
        public WrapperLock( final K key ) {

            this.key = key;

        }

        @Override
        public @Nullable AcquiredLock tryAcquire() {

            return AbstractLockMap.this.tryAcquire( key );

        }

    }

    /**
     * Base lock implementation.
     *
     * @since 1.0
     */
    protected abstract class AbstractMapAcquiredLock extends AbstractAcquiredLock {

        /** The key the lock is for. */
        protected final K key;

        /**
         * Initializes a lock.
         *
         * @param key The lock's key.
         */
        public AbstractMapAcquiredLock( final K key ) {

            this.key = key;

        }

    } 
    
}
