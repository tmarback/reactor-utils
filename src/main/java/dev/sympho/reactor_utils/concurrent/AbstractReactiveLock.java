package dev.sympho.reactor_utils.concurrent;

import reactor.core.publisher.Mono;

/**
 * Base implementation for a {@link ReactiveLock reactive lock}.
 *
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractReactiveLock implements ReactiveLock {

    /**
     * Acquires the, starting as soon as this method is called (instead of waiting 
     * for subscription).
     * 
     * <p>The returned Mono is subscribed to <i>once</i> per lock acquisition.
     *
     * @return A Mono that issues the lock once acquired.
     */
    protected abstract Mono<AcquiredLock> doAcquire();

    @Override
    public Mono<AcquiredLock> acquire() {

        return Mono.defer( () -> doAcquire() );

    }
    
}
