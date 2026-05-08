package com.optimatch.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// shared background executor for long tasks (the GA run)
// App.stop() calls shutdown() once on exit
public final class AppLifecycle {

    private static ExecutorService backgroundExecutor;

    // utility class, no instances
    private AppLifecycle() {
    }

    // lazily create a single-threaded daemon executor
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

    // stop running tasks, wait briefly, then drop the executor
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
