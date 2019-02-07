package io.electrica.pipeline.java8.spi;

import io.electrica.sdk.java8.api.Electrica;

import java.util.concurrent.CountDownLatch;

public abstract class BackgroundProcessLambda implements Lambda {

    private final CountDownLatch stopSignalLatch = new CountDownLatch(1);

    @Override
    public void onStopSignal() {
        stopSignalLatch.countDown();
    }

    @Override
    public void initialize(Electrica electrica) throws Exception {
        // Add message listeners here
    }

    @Override
    public void doWork(Electrica electrica) throws Exception {
        stopSignalLatch.await();
    }

    @Override
    public void destroy(Electrica electrica) throws Exception {
        // Stop and remove message listeners here
    }
}
