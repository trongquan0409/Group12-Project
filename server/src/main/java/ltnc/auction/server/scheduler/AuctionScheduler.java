package ltnc.auction.server.scheduler;

import ltnc.auction.server.service.AuctionManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AuctionScheduler implements AutoCloseable {
    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "auction-scheduler");
                thread.setDaemon(true);
                return thread;
            });

    public void start(AuctionManager manager) {
        executorService.scheduleWithFixedDelay(() -> {
            try {
                manager.refreshStatuses();
            } catch (Exception exception) {
                System.err.println("Scheduler error: " + exception.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
