package io.electrica.pipeline.java8.spi;

import io.electrica.sdk.java8.api.Electrica;

public interface Lambda {

    /**
     * Lambda name, that will be propagated as Electrica instance name.
     * <p>
     * Should be uniques within pipeline.
     */
    String getName();

    /**
     * Invoked when pipeline or particular lambda get stop signal.
     * <p>
     * Have to implement graceful lambda stop process of {@link #doWork(Electrica)} method.
     * Guaranteed that this method won't invoke before {@link #initialize(Electrica)}.
     */
    void onStopSignal() throws Exception;

    /**
     * Should be used to initialize lambda before start main job {@link #doWork(Electrica)} method.
     * <p>
     * Haven't contains any block or long operations. Guaranteed that this, {@link #doWork(Electrica)}
     * and {@link #destroy(Electrica)} methods will be executed in the same thread.
     * <p>
     * In case of thrown exception lambda won't started.
     */
    default void initialize(Electrica electrica) throws Exception {
        // nop implementation
    }

    /**
     * The main lambda job should be executed here.
     * <p>
     * This method have to occupy current thread until main job will finish. Any long or block operations
     * have to be executed here.
     * <p>
     * Lambda considered as finished after this method will executed or exception thrown.
     */
    void doWork(Electrica electrica) throws Exception;

    /**
     * Method to implement some actions to gracefully stop lambda and release resources. E.q. stop and
     * remove message listeners.
     * <p>
     * Invoked after {@link #doWork(Electrica)} even if exception has been thrown.
     */
    default void destroy(Electrica electrica) throws Exception {
        // nop implementation
    }
}
