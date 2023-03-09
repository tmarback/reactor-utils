package dev.sympho.reactor_utils.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import reactor.core.publisher.Mono;

/**
 * Reactive lock map that asychronously waits for a lock to become available.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 */
public class AsyncLockMap<K extends @NonNull Object> extends AbstractReactiveLockMap<K> {

    /** Pseudo-lock map. */
    private final ConcurrentMap<K, Mono<Void>> locks;

    /**
     * Creates a new instance.
     */
    public AsyncLockMap() {

        locks = new ConcurrentHashMap<>();

    }

    @Override
    public @Nullable AcquiredLock tryAcquire( final K key ) {

        final var lock = new MapAcquiredLock( key );
        if ( locks.putIfAbsent( key, lock.doneMono() ) == null ) {
            return lock;
        } else {
            return null;
        }

    }

    @Override
    protected Mono<AcquiredLock> doAcquire( final K key ) {

        final var lock = new MapAcquiredLock( key );
        final var ready = locks.put( key, lock.doneMono() );

        final Mono<AcquiredLock> mono;
        if ( ready != null ) {
            mono = ready.thenReturn( lock );
        } else {
            mono = Mono.just( lock );
        }

        return mono.doOnCancel( () -> mono.subscribe( AcquiredLock::release ) );

    }

    /**
     * The lock implementation.
     *
     * @since 1.0
     */
    private final class MapAcquiredLock extends AbstractReactiveAcquiredLock {

        /** The key the lock is for. */
        private final K key;

        /**
         * Initializes a lock.
         *
         * @param key The lock's key.
         */
        MapAcquiredLock( final K key ) {

            this.key = key;

        }

        @Override
        protected void markReleased() {

            locks.remove( key, doneMono() ); // Only change map if still latest

        }

    } 
    
}
