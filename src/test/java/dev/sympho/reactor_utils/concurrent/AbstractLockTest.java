package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base for a {@link Lock} test suite.
 *
 * @param <T> The implementation type.
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractLockTest<T extends Lock> implements LockTest {

    /** The instance under test. */
    protected T lock;

    /**
     * Creates the lock under test.
     *
     * @return The instance under test.
     */
    public abstract T makeLock();

    /**
     * Initializes the IUT.
     */
    @BeforeEach
    public void initialize() {

        lock = makeLock();

    }

    @Override
    public @Nullable AcquiredLock tryAcquire() {

        return lock.tryAcquire();

    }
    
}
