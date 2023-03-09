package dev.sympho.reactor_utils.concurrent;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Base implementation for an acquired reactive lock, where release is signaled to waiting
 * tasks through a {@link #doneMono() Mono}.
 *
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractReactiveAcquiredLock extends AbstractAcquiredLock {

    /** Sink that indicates completion to the next in queue. */
    private final Sinks.Empty<Void> completion = Sinks.empty();

    /**
     * Returns a Mono that completes when the lock is released.
     *
     * @return The Mono. Always returns the same instance.
     */
    public Mono<Void> doneMono() {

        return completion.asMono();

    }

    /**
     * Marks the underlying lock as released.
     */
    protected abstract void markReleased();

    @Override
    protected void doRelease() {

        markReleased();
        completion.tryEmitEmpty(); // Signal completion

    }
    
}
