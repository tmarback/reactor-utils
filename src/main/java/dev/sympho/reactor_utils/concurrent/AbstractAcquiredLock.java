package dev.sympho.reactor_utils.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base implementation for acquired locks.
 *
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAcquiredLock implements AcquiredLock {

    /** Whether the lock is currently active. */
    private final AtomicBoolean active = new AtomicBoolean( true );

    /**
     * Performs the actual release of the lock.
     */
    protected abstract void doRelease();

    @Override
    public void release() {

        if ( active.getAndSet( false ) ) {
            doRelease(); // Only release if currently active
        }

    }
    
}
