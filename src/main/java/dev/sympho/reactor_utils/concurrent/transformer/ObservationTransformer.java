package dev.sympho.reactor_utils.concurrent.transformer;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

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
     * @param value The tag value. If {@code null}, it does not add the tag.
     * @return The created function.
     */
    private <T extends @NonNull Object> Function<Mono<T>, Mono<T>> highCardinalityTag( 
            final String key, final @Nullable String value ) {

        if ( value == null ) {
            return Function.identity();
        }

        return mono -> mono.doOnSubscribe( s -> addHighCardinalityTag( key, value ) );

    }

    @Override
    public Mono<AcquiredLock> transformAcquire( final Mono<AcquiredLock> pending ) {

        return pending.name( "lock.acquire" )
                .tag( "lock.name", name )
                .tap( Micrometer.observation( registry ) );

    }

    /**
     * @param key The lock key. If non-{@code null}, it is added to the observation as a
     *            high-cardinality label.
     * @see #keyMapped(Function)
     * @see #noKey()
     * @implNote As lock maps cannot have {@code null} keys, generally the key will <i>always</i>
     *           be added as a label. If it is not useful or too high cardinality, use
     *           {@link #keyMapped(Function)} to reduce the label space or {@link #noKey()} to
     *           prevent it from being added, or remove the {@code lockmap.key} label using 
     *           a filter.
     */
    @Override
    @SuppressWarnings( "argument" ) // Weird inference
    public Mono<AcquiredLock> transformAcquire( 
            final @Nullable String key, final Mono<AcquiredLock> pending ) {

        return pending.name( "lockmap.acquire" )
                .tag( "lockmap.name", name )
                .transform( highCardinalityTag( "lockmap.key", key ) )
                .tap( Micrometer.observation( registry ) );

    }

    /**
     * Derives a lock map transformer that uses the given mapper function to convert
     * keys into strings before delegating to this transformer.
     *
     * @param <K> The key type.
     * @param keyMapper The function to use to transform keys. It may return {@code null}, in
     *                  which case the key is not used as a label.
     * @return The transformer.
     * @see #transformAcquire(String, Mono)
     * @apiNote The primary purpose of this method is having a way to add observability to lock
     *          maps with non-string keys; however, it may also be used with string-keyed maps
     *          in order to apply arbitrary transformations to the key before using it as a label
     *          (for example, filtering out certain key values or mapping multiple keys to one
     *          label to reduce the cardinality).
     */
    @SideEffectFree
    public <K extends @NonNull Object> LockMapTransformer<K> keyMapped( 
            final Function<K, @Nullable String> keyMapper ) {

        return ( k, p ) -> transformAcquire( keyMapper.apply( k ), p );

    }

    /**
     * Derives a lock map transformer that does not add the lock key as a label in the
     * observation.
     *
     * @param <K> The key type.
     * @return The transformer.
     * @see #transformAcquire(String, Mono)
     */
    @SideEffectFree
    public <K extends @NonNull Object> LockMapTransformer<K> noKey() {

        return ( k, p ) -> transformAcquire( null, p );

    }

    @Override
    public Mono<Void> transformAwait( final Mono<Void> pending ) {

        return pending.name( "latch.await" )
                .tag( "latch.name", name )
                .tap( Micrometer.observation( registry ) );

    }
    
}
