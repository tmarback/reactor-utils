package dev.sympho.reactor_utils.concurrent;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Test driver for {@link ReactiveBreaker}.
 *
 * @version 1.0
 * @since 1.0
 */
public class BreakerTest {

    /** The breaker under test. */
    private ReactiveBreaker dut;

    /**
     * Creates the DUT.
     */
    @BeforeEach
    public void setUp() {

        dut = new ReactiveBreaker();

    }

    /**
     * Prepares the validator mono.
     *
     * @param validator The validator store. It should contain {@code true} to start.
     * @return The validator mono.
     */
    private Mono<Boolean> prepareValidator( final AtomicBoolean validator ) {

        return Mono.just( false )
                .delayElement( Duration.ofSeconds( 30 ) )
                .doOnNext( r -> validator.set( false ) );

    }

    /**
     * Tests that the breaker does not interfere with monitored monos if it is not shut off.
     */
    @Test
    public void testNoBreak() {

        final var result = new AtomicBoolean( false );
        final var validator = new AtomicBoolean( true );

        StepVerifier.withVirtualTime( () -> Mono.just( true )
                        .delayElement( Duration.ofSeconds( 3 ) )
                        .doOnNext( r -> result.set( r ) )
                        .transform( dut::monitor )
                        .mergeWith( prepareValidator( validator ) )
                )
                .expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 3 ) )
                .expectNext( true )
                .expectNoEvent( Duration.ofSeconds( 27 ) )
                .expectNext( false )
                .verifyComplete();

        assertThat( result.get() ).isTrue();
        assertThat( validator.get() ).isFalse();

    }

    /**
     * Tests that the breaker cancels a single mono that it observes.
     */
    @Test
    public void testOne() {

        final var result = new AtomicBoolean( false );
        final var validator = new AtomicBoolean( true );
        
        StepVerifier.withVirtualTime( () -> Mono.just( true )
                        .delayElement( Duration.ofSeconds( 3 ) )
                        .doOnNext( r -> result.set( r ) )
                        .transform( dut::monitor )
                        .mergeWith( prepareValidator( validator ) )
                )
                .expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 2 ) )
                .then( dut::shutoff )
                .expectNoEvent( Duration.ofSeconds( 28 ) )
                .expectNext( false )
                .verifyComplete();

        assertThat( result.get() ).isFalse();
        assertThat( validator.get() ).isFalse();

    }

    /**
     * Tests that the breaker cancels many monos observed by it, while not interfering
     * with monitored monos that complete before it is shut off.
     */
    @Test
    public void testMany() {

        final var result1 = new AtomicBoolean( false );
        final var result2 = new AtomicBoolean( false );
        final var result3 = new AtomicBoolean( false );
        final var result4 = new AtomicBoolean( false );
        final var validator = new AtomicBoolean( true );
        
        StepVerifier.withVirtualTime( () -> Flux.merge(

                    Mono.just( true )
                        .delayElement( Duration.ofSeconds( 3 ) )
                        .doOnNext( r -> result1.set( r ) )
                        .transform( dut::monitor ),
                    
                    Mono.just( true )
                        .delayElement( Duration.ofSeconds( 6 ) )
                        .doOnNext( r -> result2.set( r ) )
                        .transform( dut::monitor ),

                    Mono.just( true )
                        .delayElement( Duration.ofSeconds( 10 ) )
                        .doOnNext( r -> result3.set( r ) )
                        .transform( dut::monitor ),

                    Mono.just( true )
                        .delayElement( Duration.ofSeconds( 15 ) )
                        .doOnNext( r -> result4.set( r ) )
                        .transform( dut::monitor ),
                    
                    prepareValidator( validator )

                ) )
                .expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 3 ) )
                .expectNext( true )
                .expectNoEvent( Duration.ofSeconds( 3 ) )
                .expectNext( true )
                .expectNoEvent( Duration.ofSeconds( 2 ) )
                .then( dut::shutoff )
                .expectNoEvent( Duration.ofSeconds( 22 ) )
                .expectNext( false )
                .verifyComplete();

        assertThat( result1.get() ).isTrue();
        assertThat( result2.get() ).isTrue();
        assertThat( result3.get() ).isFalse();
        assertThat( result4.get() ).isFalse();
        assertThat( validator.get() ).isFalse();

    }

    /**
     * Tests that calling {@link ReactiveBreaker#shutoff()} multiple times has no effect.
     */
    @Test
    public void testIdempotent() {

        final var result = new AtomicBoolean( false );
        final var validator = new AtomicBoolean( true );
        
        StepVerifier.withVirtualTime( () -> Mono.just( true )
                        .delayElement( Duration.ofSeconds( 3 ) )
                        .doOnNext( r -> result.set( r ) )
                        .transform( dut::monitor )
                        .mergeWith( prepareValidator( validator ) )
                )
                .expectSubscription()
                .expectNoEvent( Duration.ofSeconds( 2 ) )
                .then( dut::shutoff )
                .then( dut::shutoff )
                .then( dut::shutoff )
                .expectNoEvent( Duration.ofSeconds( 28 ) )
                .expectNext( false )
                .verifyComplete();

        assertThat( result.get() ).isFalse();
        assertThat( validator.get() ).isFalse();
        
    }
    
}
