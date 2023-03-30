package dev.sympho.reactor_utils.concurrent;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.reactivestreams.Publisher;

import dev.sympho.reactor_utils.concurrent.transformer.LatchTransformer;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * An equivalent to Java's {@link CountDownLatch} that uses Reactive Streams for non-blocking
 * waiting. It can be used by either directly subscribing to {@link #await()} or by using an
 * instance of this class with {@link Mono#delayUntil(Function)} (or the Flux equivalent).
 * 
 * <p>The latch can also be failed, propagating an error to those waiting on it instead of
 * a completion.
 *
 * @see CountDownLatch
 * @version 1.0
 * @since 1.0
 */
public class ReactiveLatch implements Function<Object, Publisher<Void>> {

    /** The backing sink. */
    private final Sinks.Empty<Void> sink;

    /** The counter. */
    private final AtomicLong count;

    /** Transformer applied to the await mono before returning it. */
    private final LatchTransformer transformer;

    /**
     * Creates a new latch that opens after counting down a given number of times.
     *
     * @param needed How many times {@link #countDown()} needs to be called before
     *               the latch opens.
     * @throws IllegalArgumentException if the given value is not positive (>0).
     */
    @SideEffectFree
    public ReactiveLatch( final long needed ) throws IllegalArgumentException {

        this( needed, m -> m );

    }

    /**
     * Creates a new latch that opens after counting down a given number of times.
     *
     * @param needed How many times {@link #countDown()} needs to be called before
     *               the latch opens.
     * @param transformer A transform function to apply to the result of 
     *                    {@link #await()} before returning it.
     * @throws IllegalArgumentException if the given value is not positive (>0).
     */
    @SideEffectFree
    public ReactiveLatch( final long needed, final LatchTransformer transformer )
            throws IllegalArgumentException {

        if ( needed <= 0 ) {
            throw new IllegalArgumentException( "Latch must require a positive number." );
        }

        this.sink = Sinks.empty();
        this.count = new AtomicLong( needed );
        this.transformer = Objects.requireNonNull( transformer );

    }

    /**
     * Creates a new latch that opens after {@link #countDown()} is called once. This is
     * effectively a binary latch or a switch.
     */
    @SideEffectFree
    public ReactiveLatch() {

        this( 1L );

    }

    /**
     * Creates a new latch that opens after {@link #countDown()} is called once. This is
     * effectively a binary latch or a switch.
     *
     * @param transformer A transform function to apply to the result of 
     *                    {@link #await()} before returning it.
     */
    @SideEffectFree
    public ReactiveLatch( final LatchTransformer transformer ) {

        this( 1L, transformer );

    }

    /**
     * Creates a publisher that completes once this latch opens.
     */
    @Pure
    @Override
    public Publisher<Void> apply( final Object ignored ) {

        return await();

    }

    /**
     * Creates a Mono that emits a completion signal when this latch enters the open state.
     * 
     * <p>Once the latch is open, subscribing to the returned Mono will complete instantly.
     *
     * @return The Mono to wait on.
     */
    @Pure
    public Mono<Void> await() {

        return sink.asMono()
                .transform( transformer::transformAwait );

    }

    /**
     * Counts down the latch. If the internal counter reaches zero, the latch opens.
     * 
     * <p>Has no effect if the latch is already opened or failed.
     *
     * @throws IllegalStateException if emitting the completion signal failed.
     */
    public void countDown() throws IllegalStateException {

        if ( count.decrementAndGet() == 0 ) { // Only the call that reaches zero emits
            final var result = sink.tryEmitEmpty();
            if ( result.isFailure() ) {
                throw new IllegalStateException( "Releasing latch failed: " + result );
            }
        }

    }

    /**
     * Fails the latch, issuing an error signal to those waiting on it.
     * 
     * <p>Has no effect if the latch is already opened or failed.
     *
     * @param error The error to issue.
     * @throws IllegalStateException if emitting the error signal failed.
     */
    public void fail( final Throwable error ) throws IllegalStateException {

        if ( count.getAndSet( 0 ) > 0 ) { // Only do it if not open yet
            final var result = sink.tryEmitError( error );
            if ( result.isFailure() ) {
                throw new IllegalStateException( "Issuing error on latch failed: " + result );
            }
        }

    }
    
}
