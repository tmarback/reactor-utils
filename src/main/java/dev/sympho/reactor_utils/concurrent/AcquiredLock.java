package dev.sympho.reactor_utils.concurrent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A lock that has been acquired.
 * 
 * <p>Unlike a normal lock, the code that has a reference to an instance already owns 
 * the corresponding lock and can only release it. Once released, the lock may not 
 * be re-acquired through the same instance, instead a new instance must be obtained 
 * from the original source.
 * 
 * <p>Releasing a lock (using any of the provided methods) is a <i>idempotent</i>
 * operation; it is released on the first call, and any subsequent calls (even to
 * different methods) have no effect.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote While binding the ownership of a lock to an instance instead of a thread
 *          arguably makes it harder to achieve concurrency safety (due to it still
 *          being possible to make concurrent accesses after acquiring the lock, as
 *          it is not bound to a particular thread), it is necessary in the context
 *          of reactive streams where a single chain of operations may execute in
 *          different threads.
 */
public interface AcquiredLock extends AutoCloseable {

    /**
     * Releases the held lock, making it available to be acquired again.
     */
    void release();

    /**
     * Composes the release of this lock with the given Mono so that the lock is
     * released after the operation completes.
     * 
     * <p>Note that the release occurs <i>after</i> the completion is propagated
     * downstream. Do not assume that the lock is already available when the
     * returned Mono terminates.
     *
     * @param <T> The data type.
     * @param source The data source.
     * @return A Mono that relays the data emitted by the source, and releases
     *         the lock once it terminates for any reason (including cancellation).
     */
    default <T> Mono<T> releaseAfter( final Mono<T> source ) {

        return source.doFinally( s -> release() );

    }

    /**
     * Composes the release of this lock with the given Flux so that the lock is
     * released after the operation completes.
     * 
     * <p>Note that the release occurs <i>after</i> the completion is propagated
     * downstream. Do not assume that the lock is already available when the
     * returned Mono terminates.
     *
     * @param <T> The data type.
     * @param source The data source.
     * @return A Flux that relays the data emitted by the source, and releases
     *         the lock once it terminates for any reason (including cancellation).
     */
    default <T> Flux<T> releaseAfter( final Flux<T> source ) {

        return source.doFinally( s -> release() );

    }

    /**
     * Releases the lock.
     *
     * @see #release()
     */
    @Override
    default void close() {

        release();

    }

}
