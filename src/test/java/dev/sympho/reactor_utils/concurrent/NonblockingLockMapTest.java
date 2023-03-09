package dev.sympho.reactor_utils.concurrent;

import org.junit.jupiter.api.Nested;

/**
 * Test driver for {@link NonblockingLockMap}.
 *
 * @version 1.0
 * @since 1.0
 */
public class NonblockingLockMapTest extends AbstractLockMapTest<NonblockingLockMap<String>> {

    @Override
    public NonblockingLockMap<String> makeLocks() {

        return new NonblockingLockMap<>();

    }

    /**
     * Tests for the map itself.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    @Nested
    public class MapTest extends AbstractMapTest {}

    /**
     * Tests for a lock backed by the map.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    @Nested
    public class MapLockTest extends AbstractMapLockTest {}
    
}
