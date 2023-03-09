package dev.sympho.reactor_utils.concurrent;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for a {@link Lock} implementation.
 *
 * @version 1.0
 * @since 1.0
 */
public interface LockTest {

    /**
     * Tries to acquire the lock.
     *
     * @return The acquired lock, or {@code null} if it is not available.
     * @see Lock#tryAcquire()
     */
    @Nullable AcquiredLock tryAcquire();

    /**
     * Tries to acquire the lock and ensures it succeeds.
     *
     * @return The acquired lock.
     */
    default AcquiredLock trySucceed() {

        final var lock = tryAcquire();
        assertThat( lock ).isNotNull();
        return lock;

    }

    /**
     * Tries to acquire the lock and ensures it fails.
     */
    default void tryFail() {

        assertThat( tryAcquire() ).isNull();

    }

    /**
     * Tests trying to acquire available lock.
     */
    @Test
    default void testTryAvailable() {

        trySucceed();

    }

    /**
     * Tests trying to acquire busy lock.
     */
    @Test
    default void testTryBusy() {

        trySucceed();
        tryFail();

    }

    /**
     * Tests acquiring and releasing a lock using try-acquire.
     */
    @Test
    default void testTryRelease() {

        final var lock = trySucceed();
        tryFail();

        lock.release();

        trySucceed();
        tryFail();

    }

    /**
     * Tests acquiring/releasing a lock from within multiple concurrent tasks.
     */
    @Test
    default void testTryInterleaved() {

        StepVerifier.withVirtualTime( () -> {

            final Mono<Integer> task1 = Mono.delay( Duration.ofSeconds( 1 ) )
                    .mapNotNull( c -> trySucceed() )
                    .delayElement( Duration.ofSeconds( 4 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 1 );

            final Mono<Integer> task2 = Mono.delay( Duration.ofSeconds( 3 ) )
                    .doOnNext( c -> tryFail() )
                    .delayElement( Duration.ofSeconds( 3 ) )
                    .mapNotNull( c -> trySucceed() )
                    .delayElement( Duration.ofSeconds( 5 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 2 );
            
            final Mono<Integer> task3 = Mono.delay( Duration.ofSeconds( 2 ) )
                    .doOnNext( c -> tryFail() )
                    .delayElement( Duration.ofSeconds( 5 ) )
                    .doOnNext( c -> tryFail() )
                    .delayElement( Duration.ofSeconds( 6 ) )
                    .mapNotNull( c -> trySucceed() )
                    .delayElement( Duration.ofSeconds( 3 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 3 );

            return Flux.merge( task1, task2, task3 );

        } ).expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 5 ) )
                .expectNext( 1 )
                .expectNoEvent( Duration.ofSeconds( 6 ) )
                .expectNext( 2 )
                .expectNoEvent( Duration.ofSeconds( 5 ) )
                .expectNext( 3 )
                .verifyComplete();

    }

    /**
     * Tests that releasing a lock is an idempotent operation (i.e. that releasing it again
     * does not interfere with future acquisitions).
     */
    @Test
    default void testIdempotentRelease() {

        final var lock1 = trySucceed();
        lock1.release();

        final var lock2 = trySucceed();
        tryFail();

        lock1.release();
        tryFail();

        lock2.release();
        trySucceed();

    }
    
}
