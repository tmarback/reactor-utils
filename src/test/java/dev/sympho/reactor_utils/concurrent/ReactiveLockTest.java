package dev.sympho.reactor_utils.concurrent;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for a {@link ReactiveLock} implementation.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ReactiveLockTest extends LockTest {

    /**
     * Acquires the lock.
     *
     * @return A Mono that issues the lock once acquired.
     * @see ReactiveLock#acquire()
     */
    Mono<AcquiredLock> acquire();

    /**
     * Tests acquiring a lock that is already available.
     */
    @Test
    default void testAcquire() {

        StepVerifier.create( acquire() )
                .expectNextCount( 1 )
                .verifyComplete();

    }

    /**
     * Tests releasing a lock after other tasks start waiting on it.
     */
    @Test
    default void testReleaseAfter() {

        StepVerifier.withVirtualTime( () -> {

            final Mono<Integer> task1 = acquire()
                    .delayElement( Duration.ofSeconds( 5 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 1 );

            final Mono<Integer> task2 = acquire()
                    .delaySubscription( Duration.ofSeconds( 1 ) )
                    .delayElement( Duration.ofSeconds( 3 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 2 );

            final Mono<Integer> task3 = acquire()
                    .delaySubscription( Duration.ofSeconds( 2 ) )
                    .delayElement( Duration.ofSeconds( 4 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 3 );

            return Flux.merge( task1, task2, task3 );

        } ).expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 5 ) )
                .expectNext( 1 )
                .expectNoEvent( Duration.ofSeconds( 3 ) )
                .expectNext( 2 )
                .expectNoEvent( Duration.ofSeconds( 4 ) )
                .expectNext( 3 )
                .verifyComplete();

    }

    /**
     * Tests releasing a lock before other tasks start waiting on it.
     */
    @Test
    default void testReleaseBefore() {

        StepVerifier.withVirtualTime( () -> {

            final Mono<Integer> task1 = acquire()
                    .delayElement( Duration.ofSeconds( 1 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 1 );

            final Mono<Integer> task2 = acquire()
                    .delaySubscription( Duration.ofSeconds( 3 ) )
                    .delayElement( Duration.ofSeconds( 2 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 2 );

            final Mono<Integer> task3 = acquire()
                    .delaySubscription( Duration.ofSeconds( 6 ) )
                    .delayElement( Duration.ofSeconds( 4 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 3 );

            return Flux.merge( task1, task2, task3 );

        } ).expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 1 ) )
                .expectNext( 1 )
                .expectNoEvent( Duration.ofSeconds( 4 ) )
                .expectNext( 2 )
                .expectNoEvent( Duration.ofSeconds( 5 ) )
                .expectNext( 3 )
                .verifyComplete();

    }

    /**
     * Tests releasing a lock at the same time other tasks start waiting on it.
     */
    @Test
    default void testReleaseSimultaneous() {

        StepVerifier.withVirtualTime( () -> {

            final Mono<Integer> task1 = acquire()
                    .delayElement( Duration.ofSeconds( 3 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 1 );

            final Mono<Integer> task2 = acquire()
                    .delaySubscription( Duration.ofSeconds( 3 ) )
                    .delayElement( Duration.ofSeconds( 2 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 2 );

            final Mono<Integer> task3 = acquire()
                    .delaySubscription( Duration.ofSeconds( 5 ) )
                    .delayElement( Duration.ofSeconds( 4 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 3 );

            return Flux.merge( task1, task2, task3 );

        } ).expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 3 ) )
                .expectNext( 1 )
                .expectNoEvent( Duration.ofSeconds( 2 ) )
                .expectNext( 2 )
                .expectNoEvent( Duration.ofSeconds( 4 ) )
                .expectNext( 3 )
                .verifyComplete();

    }

    /**
     * Tests that releasing a lock is an idempotent operation (i.e. that releasing it again
     * does not interfere with future acquisitions).
     */
    @Test
    default void testIdempotentReleaseReactive() {

        StepVerifier.withVirtualTime( () -> {

            final Mono<Integer> task1 = acquire()
                    .delayElement( Duration.ofSeconds( 5 ) )
                    .doOnNext( AcquiredLock::release )
                    .delayElement( Duration.ofSeconds( 1 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 1 );

            final Mono<Integer> task2 = acquire()
                    .delaySubscription( Duration.ofSeconds( 1 ) )
                    .delayElement( Duration.ofSeconds( 3 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 2 );

            final Mono<Integer> task3 = acquire()
                    .delaySubscription( Duration.ofSeconds( 2 ) )
                    .delayElement( Duration.ofSeconds( 4 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 3 );

            return Flux.merge( task1, task2, task3 );

        } ).expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 6 ) )
                .expectNext( 1 )
                .expectNoEvent( Duration.ofSeconds( 2 ) )
                .expectNext( 2 )
                .expectNoEvent( Duration.ofSeconds( 4 ) )
                .expectNext( 3 )
                .verifyComplete();

    }

    /**
     * Tests that subscribing to the lock mono multiple times causes distinct acquisitions.
     */
    @Test
    default void testReusableMono() {

        StepVerifier.withVirtualTime( () -> {

            final Mono<AcquiredLock> mono = acquire();

            final Mono<Integer> task1 = mono
                    .delayElement( Duration.ofSeconds( 5 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 1 );

            final Mono<Integer> task2 = mono
                    .delaySubscription( Duration.ofSeconds( 1 ) )
                    .delayElement( Duration.ofSeconds( 3 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 2 );

            final Mono<Integer> task3 = mono
                    .delaySubscription( Duration.ofSeconds( 2 ) )
                    .delayElement( Duration.ofSeconds( 4 ) )
                    .doOnNext( AcquiredLock::release )
                    .thenReturn( 3 );

            return Flux.merge( task1, task2, task3 );

        } ).expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 5 ) )
                .expectNext( 1 )
                .expectNoEvent( Duration.ofSeconds( 3 ) )
                .expectNext( 2 )
                .expectNoEvent( Duration.ofSeconds( 4 ) )
                .expectNext( 3 )
                .verifyComplete();

    }
    
}
