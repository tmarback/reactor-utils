package dev.sympho.reactor_utils.concurrent.transformer;

import dev.sympho.reactor_utils.concurrent.ReactiveLatch;
import reactor.core.publisher.Mono;

/**
 * A transformer for the output of waiting on a latch.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface LatchTransformer {

    /**
     * Transforms the output of {@link ReactiveLatch#await()}.
     *
     * @param pending The raw output.
     * @return The transformed output.
     */
    Mono<Void> transformAwait( Mono<Void> pending );
    
}
