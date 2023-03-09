package dev.sympho.reactor_utils.concurrent;

import reactor.core.publisher.Mono;

/**
 * Base for a {@link ReactiveLock} test suite.
 *
 * @param <T> The implementation type.
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractReactiveLockTest<T extends ReactiveLock> extends AbstractLockTest<T>
        implements ReactiveLockTest {

    @Override
    public Mono<AcquiredLock> acquire() {

        return lock.acquire();

    }
    
}
