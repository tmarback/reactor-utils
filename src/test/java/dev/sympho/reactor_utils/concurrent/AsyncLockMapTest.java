package dev.sympho.reactor_utils.concurrent;

import org.junit.jupiter.api.Nested;

/**
 * Test driver for {@link AsyncLockMap}.
 *
 * @version 1.0
 * @since 1.0
 */
public class AsyncLockMapTest extends AbstractReactiveLockMapTest<AsyncLockMap<String>> {

    @Override
    public AsyncLockMap<String> makeLocks() {

        return new AsyncLockMap<>();

    }

    /**
     * Tests for the map itself.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    @Nested
    public class MapTest extends AbstractReactiveMapTest {}

    /**
     * Tests for a lock backed by the map.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    @Nested
    public class MapLockTest extends AbstractReactiveMapLockTest {}
    
}
