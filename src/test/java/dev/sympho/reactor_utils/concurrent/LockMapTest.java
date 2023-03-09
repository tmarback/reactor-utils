package dev.sympho.reactor_utils.concurrent;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests for a {@link LockMap} implementation.
 *
 * @version 1.0
 * @since 1.0
 */
public interface LockMapTest extends LockTest {

    /** Key to use in tests with only one lock. */
    String KEY = "lorikeet";
    /** First set of keys to use in tests. */
    List<String> KEYS_1 = List.of( "banana", "potato", "island" );
    /** Second set of keys to use in tests. */
    List<String> KEYS_2 = List.of( "truck", "plane" );
    /** Third set of keys to use in tests. */
    List<String> KEYS_3 = List.of( "ocean", "split" );

    /**
     * Tries to acquire the lock with the given key.
     *
     * @param key The lock's key.
     * @return The acquired lock, or {@code null} if it is not available.
     * @see LockMap#tryAcquire()
     */
    @Nullable AcquiredLock tryAcquire( String key );

    @Override
    default @Nullable AcquiredLock tryAcquire() {

        return tryAcquire( KEY );

    }

    /**
     * Tries to acquire a sequence of locks.
     *
     * @param keys The keys to acquire locks for.
     * @return A list with the corresponding acquired locks, with {@code null} for locks
     *         that are busy and could not be acquired.
     */
    default List<AcquiredLock> tryAcquire( final List<String> keys ) {

        return keys.stream().map( this::tryAcquire ).toList();

    }

    /**
     * Tries to acquire a lock and ensures it succeeds.
     *
     * @param key The key to acquire a lock for.
     * @return The acquired lock.
     */
    default AcquiredLock trySucceed( final String key ) {

        final var lock = tryAcquire( key );
        assertThat( lock )
                .as( "For free key '%s'", key )
                .isNotNull();
        return lock;

    }

    /**
     * Tries to acquire a sequence of locks and ensures they all succeed.
     *
     * @param keys The keys to acquire locks for.
     * @return A list with the corresponding acquired locks.
     */
    default List<AcquiredLock> trySucceed( final List<String> keys ) {

        final var locks = tryAcquire( keys );
        assertThat( locks )
                .as( "For free keys %s", keys )
                .doesNotContainNull();
        return locks;

    }

    /**
     * Tries to acquire a lock and ensures it fails.
     *
     * @param key The key to acquire a lock for.
     */
    default void tryFail( final String key ) {

        assertThat( tryAcquire( key ) )
                .as( "For busy key '%s'", key )
                .isNull();

    }

    /**
     * Tries to acquire a sequence of locks and ensures they all fail.
     *
     * @param keys The keys to acquire locks for.
     */
    default void tryFail( final List<String> keys ) {

        assertThat( tryAcquire( keys ) )
                .as( "For busy keys %s", keys )
                .containsOnlyNulls();

    }

    /**
     * Tests trying to acquire available locks.
     */
    @Test
    default void testTryAvailableMany() {

        trySucceed( KEYS_1 );

    }

    /**
     * Tests trying to acquire busy locks.
     */
    @Test
    default void testTryBusyMany() {

        trySucceed( KEYS_1 );
        tryFail( KEYS_1 );

    }

    /**
     * Tests trying to acquire available and busy locks.
     */
    @Test
    default void testTryMixed() {

        trySucceed( KEYS_1 );
        tryFail( KEYS_1 );

        trySucceed( KEYS_2 );
        tryFail( ListUtils.union( KEYS_1, KEYS_2 ) );

        trySucceed( KEYS_3 );

    }

    /**
     * Tests acquiring and releasing many lock using try-acquire.
     */
    @Test
    default void testTryReleaseMany() {

        final var locks = trySucceed( KEYS_1 );
        tryFail( KEYS_1 );

        locks.forEach( AcquiredLock::release );

        trySucceed( KEYS_1 );
        tryFail( KEYS_1 );

    }

    /**
     * Tests releasing only some of the locks acquired with try-acquire.
     */
    @Test
    default void testTryReleasePartial() {

        final var locks = trySucceed( KEYS_1 );
        trySucceed( KEYS_2 );

        tryFail( ListUtils.union( KEYS_1, KEYS_2 ) );

        locks.forEach( AcquiredLock::release );

        tryFail( KEYS_2 );
        trySucceed( KEYS_1 );
        tryFail( ListUtils.union( KEYS_1, KEYS_2 ) );

    }
    
}
