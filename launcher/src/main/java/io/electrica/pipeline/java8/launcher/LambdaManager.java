package io.electrica.pipeline.java8.launcher;

import io.electrica.pipeline.java8.spi.Lambda;
import io.electrica.sdk.java8.api.Electrica;
import io.electrica.sdk.java8.api.http.HttpModule;
import io.electrica.sdk.java8.core.SingleInstanceHttpModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.concurrent.*;

@Slf4j
class LambdaManager {

    private static final long AWAIT_STOP_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    private final Lambda sourceLambda;
    private final boolean autoStart;
    private final String accessKey;
    @Nullable
    private final String apiUrl;

    private final ExecutorService executor;
    private Context context;

    LambdaManager(Lambda sourceLambda, boolean autoStart, String accessKey, @Nullable String apiUrl) {
        this.sourceLambda = sourceLambda;
        this.autoStart = autoStart;
        this.accessKey = accessKey;
        this.apiUrl = apiUrl;
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "manager-" + getName());
            thread.setDaemon(true);
            return thread;
        });
    }

    String getName() {
        return sourceLambda.getName();
    }

    private Electrica createElectrica() {
        HttpModule httpModule = apiUrl == null ?
                new SingleInstanceHttpModule() :
                new SingleInstanceHttpModule(apiUrl);
        return Electrica.instance(httpModule, accessKey);
    }

    synchronized void startLambda() throws Exception {
        if (context != null) {
            throw new IllegalStateException("Lambda already started: " + getName());
        }
        Lambda lambda = LambdaLoader.newInstanceOf(sourceLambda);
        Electrica electrica = createElectrica();
        Future<Void> future = executor.submit(() -> {
            try {
                lambda.started(electrica);
                lambda.doWork(electrica);
                lambda.beforeStop(electrica);
                return null;
            } finally {
                try {
                    electrica.close();
                } catch (Exception e) {
                    log.error("Error closing electrica instance", e);
                }
            }
        });
        context = new Context(lambda, future, electrica);
    }

    synchronized void stopLambda() throws Exception {
        if (context == null) {
            throw new IllegalStateException("Lambda not yet started: " + getName());
        }

        if (!context.getFuture().isDone()) {
            try {
                context.getLambda().onStopSignal();
            } catch (Exception e) {
                log.error("Error handling stop for lambda: " + getName(), e);
            }
        }

        try {
            context.getFuture().get(AWAIT_STOP_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            log.error("Error executing lambda: " + getName(), e);
        } catch (TimeoutException e) {
            context.getElectrica().close();
            log.error("Cannot await lambda: {}. Restart may be impossible", getName());
        }

        context = null;
    }

    void start() throws Exception {
        if (autoStart) {
            startLambda();
        }
    }

    void stop() throws Exception {
        synchronized (this) {
            if (context != null) {
                startLambda();
            }
        }
    }

    @Getter
    @AllArgsConstructor
    private static class Context {
        private final Lambda lambda;
        private final Future<Void> future;
        private final Electrica electrica;
    }
}
