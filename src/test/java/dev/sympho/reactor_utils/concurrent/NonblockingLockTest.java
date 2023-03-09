package dev.sympho.reactor_utils.concurrent;

/**
 * Test driver for {@link NonblockingLock}.
 *
 * @version 1.0
 * @since 1.0
 */
public class NonblockingLockTest extends AbstractLockTest<NonblockingLock> {

    @Override
    public NonblockingLock makeLock() {

        return new NonblockingLock();

    }
    
}
