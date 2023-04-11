package dev.sympho.reactor_utils.concurrent.transformer;

import org.checkerframework.checker.nullness.qual.NonNull;

import dev.sympho.reactor_utils.concurrent.AcquiredLock;
import dev.sympho.reactor_utils.concurrent.ReactiveLockMap;
import reactor.core.publisher.Mono;

/**
 * A transformer for the output of acquiring a lock in a lockmap.
 *
 * @param <K> The key type.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface LockMapTransformer<K extends @NonNull Object> {

    /**
     * Transforms the output of {@link ReactiveLockMap#acquire(Object)}.
     *
     * @param key The lock key.
     * @param pending The raw output.
     * @return The transformed output.
     */
    Mono<AcquiredLock> transformAcquire( K key, Mono<AcquiredLock> pending );
    
}
