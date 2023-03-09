package dev.sympho.reactor_utils.concurrent;

import java.util.function.UnaryOperator;

import org.checkerframework.checker.nullness.qual.NonNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Lock map that allows waiting for a lock to become available using reactive streams.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 */
public interface ReactiveLockMap<K extends @NonNull Object> extends LockMap<K> {

    /**
     * Acquires the lock of the given key.
     * 
     * <p>Note that each subscription to the returned Mono will be a <i>different</i> acquisition
     * of the lock, allowing the Mono to be re-used. If acquiring the lock only once for all
     * subscribers is desired, use {@link Mono#cache()} on the returned Mono, but do be careful
     * to make sure that the lock is only released when all subscribers are done.
     * 
     * <p>It is safe to cancel a subscription made to the returned Mono. If a cancellation is
     * detected, the lock will simply be re-released as soon as it is acquired. Do remember,
     * however, that a downstream cancellation will not be propagated if this stage has already
     * completed, in which case the lock must be released downstream.
     *
     * @param key The key to acquire a lock for.
     * @return A Mono that issues the lock once acquired.
     * @implSpec The current implementation operates in a FIFO manner. That is, when a lock is
     *           released, it is given to the task that has been waiting for the longest amount
     *           of time.
     */
    Mono<AcquiredLock> acquire( K key );

    @Override
    ReactiveLock get( K key );

    /**
     * Guards a Mono with the lock under the given key, acquiring the lock before 
     * subscribing to the Mono and releasing the lock when it completes. This 
     * ensures that only one subscription is active at any given time.
     *
     * @param <T> The element type.
     * @param key The key of the lock.
     * @param source The Mono to guard.
     * @return The guarded Mono.
     */
    default <T> Mono<T> guard( final K key, final Mono<T> source ) {

        return get( key ).guard( source );

    }

    /**
     * Guards a Flux with the lock under the given key, acquiring the lock before 
     * subscribing to the Flux and releasing the lock when it completes. This 
     * ensures that only one subscription is active at any given time.
     *
     * @param <T> The element type.
     * @param key The key of the lock.
     * @param source The Flux to guard.
     * @return The guarded Flux.
     */
    default <T> Flux<T> guard( final K key, final Flux<T> source ) {

        return get( key ).guard( source );

    }

    /**
     * Guards a function that guards a Mono with the lock under the given key, 
     * acquiring the lock before subscribing to the Mono and releasing the lock 
     * when it completes. This ensures that only one subscription is active at 
     * any given time.
     *
     * @param <T> The element type.
     * @param key The key of the lock.
     * @return The function that transforms Monos into guarded Monos.
     */
    default <T> UnaryOperator<Mono<T>> guardMono( final K key ) {

        return get( key )::guard;

    }

    /**
     * Guards a function that guards a Flux with the lock under the given key, 
     * acquiring the lock before subscribing to the Flux and releasing the lock 
     * when it completes. This ensures that only one subscription is active at 
     * any given time.
     *
     * @param <T> The element type.
     * @param key The key of the lock.
     * @return The function that transforms Fluxes into guarded Fluxes.
     */
    default <T> UnaryOperator<Flux<T>> guardFlux( final K key ) {

        return get( key )::guard;
        
    }
    
}
