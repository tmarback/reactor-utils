package dev.sympho.reactor_utils.concurrent;

/**
 * Test driver for {@link AsyncLock}.
 *
 * @version 1.0
 * @since 1.0
 */
public class AsyncLockTest extends AbstractReactiveLockTest<AsyncLock> {

    @Override
    public AsyncLock makeLock() {

        return new AsyncLock();

    }
    
}
