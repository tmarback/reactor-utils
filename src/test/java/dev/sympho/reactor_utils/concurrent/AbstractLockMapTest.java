package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base for a {@link LockMap} test suite.
 *
 * @param <T> The implementation type.
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractLockMapTest<T extends LockMap<String>> {

    /** The instance under test. */
    protected T map;

    /**
     * Creates the instance under test.
     *
     * @return The lock map.
     */
    public abstract T makeLocks();

    /**
     * Initializes the IUT.
     */
    @BeforeEach
    public void initialize() {

        map = makeLocks();

    }

    /**
     * Tests for the map itself.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    public abstract class AbstractMapTest implements LockMapTest {

        @Override
        public @Nullable AcquiredLock tryAcquire( final String key ) {

            return map.tryAcquire( key );

        }

    }

    /**
     * Tests for a lock backed by the map.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    public abstract class AbstractMapLockTest implements LockTest {

        /**
         * Gets the lock to test.
         *
         * @return The lock. Calling this method multiple times may return different
         *         instances, but they are always functionally the same lock.
         */
        public Lock getLock() {

            return map.get( LockMapTest.KEY );

        }

        @Override
        public @Nullable AcquiredLock tryAcquire() {

            return getLock().tryAcquire();

        }

    }

}
