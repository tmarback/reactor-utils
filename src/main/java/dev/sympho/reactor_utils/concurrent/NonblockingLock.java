package dev.sympho.reactor_utils.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import org.checkerframework.checker.nullness.qual.Nullable;

/** 
 * Lock that is purely non-blocking; an attempt to acquire it will always fail immediately
 * if the lock is currently in use.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote This is a minimal-overhead implementation to optimize use cases in which waiting for
 *          the lock is never necessary.
 */
public final class NonblockingLock implements Lock {

    /** Whether the lock is currently available. */
    private final AtomicBoolean available;
    
    /**
     * Creates a new instance.
     */
    public NonblockingLock() {

        available = new AtomicBoolean( true );

    }

    @Override
    public @Nullable AcquiredLock tryAcquire() {

        if ( available.getAndSet( false ) ) {
            return new AcquiredLockImpl();
        } else {
            return null;
        }

    }

    /**
     * The acquired lock implementation.
     *
     * @since 1.0
     */
    private final class AcquiredLockImpl extends AbstractAcquiredLock {

        /**
         * Creates a new instance.
         */
        AcquiredLockImpl() {}

        @Override
        protected void doRelease() {

            available.set( true );

        }

    }
    
}
