package dev.sympho.reactor_utils.concurrent;

import java.util.concurrent.atomic.AtomicReference;

import org.checkerframework.checker.nullness.qual.Nullable;

import reactor.core.publisher.Mono;

/**
 * Reactive lock that waits asynchronously.
 *
 * @version 1.0
 * @since 1.0
 */
public final class AsyncLock extends AbstractReactiveLock {

    /** 
     * The Mono that completes once the lock is available. 
     * {@code null} if it is currently available. 
     */
    private final AtomicReference<@Nullable Mono<Void>> pending;

    /**
     * Creates a new instance.
     */
    public AsyncLock() {

        pending = new AtomicReference<>();

    }

    @Override
    public @Nullable AcquiredLock tryAcquire() {

        final var lock = new AcquiredLockImpl();
        if ( pending.compareAndSet( null, lock.doneMono() ) ) {
            return lock;
        } else {
            return null;
        }

    }

    @Override
    public Mono<AcquiredLock> doAcquire() {

        final var lock = new AcquiredLockImpl();
        final var ready = pending.getAndSet( lock.doneMono() );

        final Mono<AcquiredLock> mono;
        if ( ready != null ) {
            mono = ready.thenReturn( lock );
        } else {
            mono = Mono.just( lock );
        }

        return mono.doOnCancel( () -> mono.subscribe( AcquiredLock::release ) );

    }

    /**
     * The acquired lock implementation.
     *
     * @since 1.0
     */
    private final class AcquiredLockImpl extends AbstractReactiveAcquiredLock {

        /**
         * Creates a new instance.
         */
        AcquiredLockImpl() {}

        @Override
        protected void markReleased() {

            pending.compareAndSet( doneMono(), null ); // Only change if still latest

        }

    }
    
}
