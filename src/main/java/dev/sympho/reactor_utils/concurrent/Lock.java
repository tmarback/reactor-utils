package dev.sympho.reactor_utils.concurrent;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple lock.
 * 
 * <p>Unlike a normal lock, the locks provided by this type are not bound to a particular
 * thread, and so any thread with a reference to the returned lock may release it.
 * 
 * <p>Be careful that this implies that the lock is <b>NOT reentrant</b>, attempting to
 * acquire a lock twice will always fail.
 *
 * @version 1.0
 * @since 1.0
 * @apiNote While binding the ownership of a lock to an instance instead of a thread
 *          arguably makes it harder to achieve concurrency safety (due to it still
 *          being possible to make concurrent accesses after acquiring the lock, as
 *          it is not bound to a particular thread), it is necessary in the context
 *          of reactive streams where a single chain of operations may execute in
 *          different threads.
 */
public interface Lock {
    
    /**
     * Acquires the lock, if currently available.
     *
     * @return The acquired lock, or {@code null} if the lock is in use and could not be acquired.
     */
    @Nullable AcquiredLock tryAcquire();

}
