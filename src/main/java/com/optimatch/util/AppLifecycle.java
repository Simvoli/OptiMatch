package com.optimatch.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Owns the shared background executor used for long-running tasks
 * (GA execution). {@link com.optimatch.App#stop()} calls {@link #shutdown()}
 * once on application exit.
 */
public final class AppLifecycle {

    private static ExecutorService backgroundExecutor;

    private AppLifecycle() {
    }

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

    public static synchronized void shutdown() {
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
