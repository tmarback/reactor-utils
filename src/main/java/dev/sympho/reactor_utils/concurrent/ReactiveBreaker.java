package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

/**
 * A circuit breaker that is capable of monitoring any number of Mono publishers, then cancel
 * all of them on demand. Note that, to downstream subscribers of that mono, it will appear as
 * if the mono completed empty.
 *
 * <p>Note also that an instance of this class is single-use only; once {@link #shutoff()} is 
 * called, any monos submitted for monitoring to the instance will be cancelled immediately.
 *
 * @version 1.0
 * @since 1.0
 */
public class ReactiveBreaker {

    /** A cached closed breaker. */
    private static final ReactiveBreaker CLOSED = new ReactiveBreaker() {

        @Override
        public void shutoff() {}

        @Override
        public <T> Mono<T> monitor( final Mono<T> mono ) {
            return Mono.empty(); // Already closed
        }

    };

    /** The signal sink. */
    private final Sinks.Empty<Void> sink;

    /** Creates a new instance. */
    public ReactiveBreaker() {

        this.sink = Sinks.empty();

    }

    /**
     * Cancels all monos monitored by this breaker.
     * 
     * <p>Calling this method on a breaker that is already shut off has no effect.
     *
     * @throws IllegalStateException if an error occurred.
     */
    @SideEffectFree // Technically can have side effects by proxy, but not the call itself
    public void shutoff() {

        final var res = sink.tryEmitEmpty();
        if ( res != EmitResult.OK && res != EmitResult.FAIL_TERMINATED ) {
            throw new IllegalStateException( "Failed to break: " + res );
        }

    }

    /**
     * Monitors the given mono.
     *
     * @param <T> The item type.
     * @param mono The mono to monitor.
     * @return The monitored mono.
     */
    @SideEffectFree
    @SuppressWarnings( "unchecked" ) // Sink is always empty, generic doesn't matter
    public <T> Mono<T> monitor( final Mono<T> mono ) {

        // Sink first in case it is already closed or closing
        return Mono.firstWithSignal( ( Mono<T> ) sink.asMono(), mono );

    }

    /**
     * Creates a breaker that is already shut off.
     *
     * @return The breaker.
     * @implSpec The current implementation uses a cached instance that always returns
     *           {@link Mono#empty()} for {@link #monitor(Mono)} and is otherwise a no-op.
     */
    @Pure
    public static ReactiveBreaker off() {

        return CLOSED;

    }
    
}
