package io.electrica.pipeline.java8.spi;

import io.electrica.sdk.java8.api.Electrica;

public interface Lambda {

    String getName();

    void onStopSignal() throws Exception;

    default void started(Electrica electrica) throws Exception {
        // nop implementation
    }

    void doWork(Electrica electrica) throws Exception;

    default void beforeStop(Electrica electrica) throws Exception {
        // nop implementation
    }
}
