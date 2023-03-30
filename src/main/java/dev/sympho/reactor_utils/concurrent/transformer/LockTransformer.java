package dev.sympho.reactor_utils.concurrent.transformer;

import dev.sympho.reactor_utils.concurrent.AcquiredLock;
import dev.sympho.reactor_utils.concurrent.ReactiveLock;
import reactor.core.publisher.Mono;

/**
 * A transformer for the output of acquiring a lock.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface LockTransformer {

    /**
     * Transforms the output of {@link ReactiveLock#acquire()}.
     *
     * @param pending The raw output.
     * @return The transformed output.
     */
    Mono<AcquiredLock> transformAcquire( Mono<AcquiredLock> pending );
    
}
