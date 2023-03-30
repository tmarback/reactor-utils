package dev.sympho.reactor_utils.concurrent.transformer;

import java.util.function.Function;

import dev.sympho.reactor_utils.concurrent.AcquiredLock;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

/**
 * A transformer that creates observations when waiting for a resource.
 * 
 * <p>Note that this class requires micrometer-observation to be in the classpath.
 *
 * @version 1.0
 * @since 1.0
 */
public class ObservationTransformer implements LockTransformer, LockMapTransformer<String>, 
        LatchTransformer {

    /** The registry to use. */
    private final ObservationRegistry registry;

    /** The name that identifies the resource. */
    private final String name;

    /**
     * Creates a new instance.
     *
     * @param registry The registry to use.
     * @param name The name that identifies the resource. Added as a tag in created observations.
     */
    public ObservationTransformer( final ObservationRegistry registry, final String name ) {

        this.registry = registry;
        this.name = name;

    }

    /**
     * Adds a high cardinality tag to the current observation (if any).
     *
     * @param key The tag key.
     * @param value The tag value.
     */
    private void addHighCardinalityTag( final String key, final String value ) {

        final var observation = registry.getCurrentObservation();
        if ( observation != null ) {
            observation.highCardinalityKeyValue( key, value );
        }

    }

    /**
     * Creates a function that adds a high cardinality tag to the current observation 
     * (if any) on subscription.
     *
     * @param <T> The mono value type.
     * @param key The tag key.
     * @param value The tag value.
     * @return The created function.
     */
    private <T> Function<Mono<T>, Mono<T>> highCardinalityTag( 
            final String key, final String value ) {

        return mono -> mono.doOnSubscribe( s -> addHighCardinalityTag( key, value ) );

    }

    @Override
    public Mono<AcquiredLock> transformAcquire( final Mono<AcquiredLock> pending ) {

        return pending.name( "lock.acquire" )
                .tag( "lock.name", name )
                .tap( Micrometer.observation( registry ) );

    }

    @Override
    public Mono<AcquiredLock> transformAcquire( 
            final String key, final Mono<AcquiredLock> pending ) {

        return pending.name( "lockmap.acquire" )
                .tag( "lockmap.name", name )
                .transform( highCardinalityTag( "lockmap.key", key ) )
                .tap( Micrometer.observation( registry ) );

    }

    @Override
    public Mono<Void> transformAwait( final Mono<Void> pending ) {

        return pending.name( "latch.await" )
                .tag( "latch.name", name )
                .tap( Micrometer.observation( registry ) );

    }
    
}
