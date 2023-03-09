package dev.sympho.reactor_utils.concurrent;

import reactor.core.publisher.Mono;

/**
 * Base for a {@link ReactiveLockMap} test suite.
 *
 * @param <T> The implementation type.
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractReactiveLockMapTest<T extends ReactiveLockMap<String>> 
        extends AbstractLockMapTest<T> {
        
    /**
     * Tests for the map itself.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    public abstract class AbstractReactiveMapTest extends AbstractMapTest 
            implements ReactiveLockMapTest {

        @Override
        public Mono<AcquiredLock> acquire( final String key ) {

            return map.acquire( key );

        }

    }

    /**
     * Tests for a lock backed by the map.
     *
     * @since 1.0
     * @apiNote This needs to be nested within the implementation class due to Surefire reports
     *          not working well with inherited nested tests. 
     */
    public abstract class AbstractReactiveMapLockTest extends AbstractMapLockTest
            implements ReactiveLockTest {

        @Override
        public ReactiveLock getLock() {

            return map.get( LockMapTest.KEY );

        }

        @Override
        public Mono<AcquiredLock> acquire() {

            return getLock().acquire();

        }

    }
    
}
