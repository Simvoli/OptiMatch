package com.optimatch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized lifecycle management for resources that must be released
 * when the JavaFX application stops (executors, DB connections, etc.).
 *
 * Components register cleanup hooks here, and {@link com.optimatch.App#stop()}
 * invokes {@link #shutdown()} once on application exit.
 */
public final class AppLifecycle {

    private static final Logger LOGGER = Logger.getLogger(AppLifecycle.class.getName());

    private static final List<Runnable> SHUTDOWN_HOOKS = new ArrayList<>();

    private static ExecutorService backgroundExecutor;

    private AppLifecycle() {
    }

    /**
     * Returns a shared single-thread daemon executor for long-running
     * background tasks (such as GA execution). Lazily initialized.
     *
     * @return the shared executor
     */
    public static synchronized ExecutorService getBackgroundExecutor() {
        if (backgroundExecutor == null || backgroundExecutor.isShutdown()) {
            backgroundExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "OptiMatch-Background");
                t.setDaemon(true);
                return t;
            });
        }
        return backgroundExecutor;
    }

    /**
     * Registers a cleanup hook to be invoked on shutdown.
     *
     * @param hook the cleanup runnable (must not throw)
     */
    public static synchronized void registerShutdownHook(Runnable hook) {
        if (hook != null) {
            SHUTDOWN_HOOKS.add(hook);
        }
    }

    /**
     * Releases all registered resources. Idempotent.
     */
    public static synchronized void shutdown() {
        for (Runnable hook : SHUTDOWN_HOOKS) {
            try {
                hook.run();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Shutdown hook failed", e);
            }
        }
        SHUTDOWN_HOOKS.clear();

        if (backgroundExecutor != null) {
            backgroundExecutor.shutdownNow();
            try {
                backgroundExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            backgroundExecutor = null;
        }
    }
}
