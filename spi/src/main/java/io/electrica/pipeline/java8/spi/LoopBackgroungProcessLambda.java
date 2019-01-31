package io.electrica.pipeline.java8.spi;

import io.electrica.sdk.java8.api.Electrica;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class LoopBackgroungProcessLambda implements Lambda {

    private static final long DEFAULT_SLEEP_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    private AtomicReference<Thread> threadReference = new AtomicReference<>();

    @Override
    public void onStopSignal() {
        threadReference.get().interrupt();
    }

    @Override
    public void initialize(Electrica electrica) throws Exception {
        threadReference.set(Thread.currentThread());
        // Add message listeners here
    }

    /**
     * Specify sleep interval for the loop.
     *
     * @return sleep interval in millis
     */
    protected long getSleepInterval() {
        return DEFAULT_SLEEP_INTERVAL;
    }

    /**
     * Execute operation in loop each {@link #getSleepInterval()} millis.
     *
     * @return {@code true} to continue loop, otherwise {@code false}
     */
    protected abstract boolean inLoop();

    @Override
    public void doWork(Electrica electrica) throws Exception {
        while (threadReference.get().isInterrupted()) {
            if (!inLoop()) {
                break;
            }
            Thread.sleep(getSleepInterval());
        }
    }

    @Override
    public void destroy(Electrica electrica) throws Exception {
        // Stop and remove message listeners here
    }
}
