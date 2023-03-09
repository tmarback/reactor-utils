package dev.sympho.reactor_utils.concurrent;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for a {@link ReactiveLockMap} implementation.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ReactiveLockMapTest extends LockMapTest, ReactiveLockTest {

    /**
     * Acquires the lock with the given key.
     *
     * @param key The lock's key.
     * @return A Mono that issues the lock once acquired.
     * @see ReactiveLockMap#acquire()
     */
    Mono<AcquiredLock> acquire( String key );

    @Override
    default Mono<AcquiredLock> acquire() {

        return acquire( KEY );

    }

    /**
     * Tests acquiring locks that are already available.
     */
    @Test
    default void testAcquireMany() {

        StepVerifier.create( Flux.concat( KEYS_1.stream().map( this::acquire ).toList() ) )
                .expectNextCount( KEYS_1.size() )
                .verifyComplete();

    }

    /**
     * Tests acquiring locks that is already available, concurrently.
     */
    @Test
    default void testAcquireConcurrent() {

        StepVerifier.create( Flux.merge( KEYS_1.stream().map( this::acquire ).toList() ) )
                .expectNextCount( KEYS_1.size() )
                .verifyComplete();

    }
    
}
