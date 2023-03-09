package dev.sympho.reactor_utils.concurrent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Lock that allows waiting for availability using reactive streams.
 * 
 * <p>As with its superinterface, this lock type is <b>NOT reentrant</b>. Waiting on a lock
 * from a task that has already acquired it <b>WILL</b> cause a deadlock.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ReactiveLock extends Lock {

    /**
     * Acquires the lock.
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
     * @return A Mono that issues the lock once acquired.
     * @implSpec The current implementation operates in a FIFO manner. That is, when a lock is
     *           released, it is given to the task that has been waiting for the longest amount
     *           of time.
     */
    Mono<AcquiredLock> acquire();

    /**
     * Guards a Mono with this lock, acquiring the lock before subscribing to the Mono
     * and releasing the lock when it completes. This ensures that only one subscription
     * is active at any given time.
     *
     * @param <T> The element type.
     * @param source The Mono to guard.
     * @return The guarded Mono.
     */
    default <T> Mono<T> guard( final Mono<T> source ) {

        return Mono.usingWhen( 
                acquire(), // Acquire lock
                l -> source, // Relay mono
                l -> Mono.fromRunnable( l::release ) // Release lock
        );

    }

    /**
     * Guards a Flux with this lock, acquiring the lock before subscribing to the Flux
     * and releasing the lock when it completes. This ensures that only one subscription
     * is active at any given time.
     *
     * @param <T> The element type.
     * @param source The Flux to guard.
     * @return The guarded Flux.
     */
    default <T> Flux<T> guard( final Flux<T> source ) {

        return Flux.usingWhen( 
                acquire(), // Acquire lock
                l -> source, // Relay flux
                l -> Mono.fromRunnable( l::release ) // Release lock
        );

    }

}
