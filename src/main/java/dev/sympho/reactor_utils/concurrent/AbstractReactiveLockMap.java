package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.checker.nullness.qual.NonNull;

import reactor.core.publisher.Mono;

/**
 * Base implementation for {@link ReactiveLockMap reactive lock maps}.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractReactiveLockMap<K extends @NonNull Object> extends AbstractLockMap<K>
        implements ReactiveLockMap<K> {

    /**
     * Acquires the lock of the given key, starting as soon as this method is called
     * (instead of waiting for subscription).
     * 
     * <p>The returned Mono is subscribed to <i>once</i> per lock acquisition.
     *
     * @param key The key to acquire a lock for.
     * @return A Mono that issues the lock once acquired.
     */
    protected abstract Mono<AcquiredLock> doAcquire( K key );

    @Override
    public Mono<AcquiredLock> acquire( final K key ) {

        return Mono.defer( () -> doAcquire( key ) );

    }

    @Override
    public ReactiveLock get( final K key ) {

        return new ReactiveWrapperLock( key );

    }

    /**
     * Overlay wrapper for a reactive lock backed by this map under a given key.
     *
     * @since 1.0
     */
    protected class ReactiveWrapperLock extends WrapperLock implements ReactiveLock {

        /**
         * Creates a new instance.
         *
         * @param key The lock key.
         */
        public ReactiveWrapperLock( final K key ) {

            super( key );

        }

        @Override
        public Mono<AcquiredLock> acquire() {

            return AbstractReactiveLockMap.this.acquire( key );

        }

    }
    
}
