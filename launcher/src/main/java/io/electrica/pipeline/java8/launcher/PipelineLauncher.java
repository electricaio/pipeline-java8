package io.electrica.pipeline.java8.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.electrica.pipeline.java8.spi.Lambda;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Main launcher to manage pipelines.
 * <p>
 * Use --help parameter to find manual.
 */
@Slf4j
public class PipelineLauncher {

    private static final String UNKNOWN_VERSION = "unknown";
    private static final String PROGRAM_NAME = "electrica-pipeline-java8-launcher";
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*" + PROGRAM_NAME + "-(?<version>.*)\\.jar!.*");

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        JCommander jCommander = JCommander.newBuilder()
                .programName(PROGRAM_NAME)
                .addObject(config)
                .build();

        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        if (config.isHelp()) {
            jCommander.usage();
        } else if (config.isVersion()) {
            JCommander.getConsole().println(getVersion());
        } else {
            CountDownLatch processLatch = new CountDownLatch(1);
            CountDownLatch processFinishedLatch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Got kill signal");
                processLatch.countDown();
                try {
                    boolean finished = processFinishedLatch.await(1, TimeUnit.MINUTES);
                    if (!finished) {
                        log.error("Can't await graceful managers finish");
                    }
                } catch (InterruptedException e) {
                    log.error("Got interrupted exception", e);
                }
            }));

            List<Lambda> lambdas = LambdaLoader.load();
            List<LambdaManager> managers = createLambdaManagers(config, lambdas);

            startManagers(managers);

            // await kill signal
            processLatch.await();
            stopManagers(managers);
            processFinishedLatch.countDown();
        }
    }

    private static List<LambdaManager> createLambdaManagers(Config config, List<Lambda> lambdas) {
        Set<String> autoStartLambdas = config.getAutoStartLambdas().stream()
                .map(String::trim)
                .collect(Collectors.toSet());
        Set<String> autoStartExcludeLambdas = config.getAutoStartExcludeLambdas().stream()
                .map(String::trim)
                .collect(Collectors.toSet());
        boolean startAll = autoStartLambdas.contains("*");

        List<LambdaManager> managers = new ArrayList<>(lambdas.size());
        for (Lambda lambda : lambdas) {
            String name = lambda.getName();
            boolean autoStart = startAll ?
                    !autoStartExcludeLambdas.contains(name) :
                    autoStartLambdas.contains(name);
            managers.add(new LambdaManager(lambda, autoStart, config.getAccessKey(), config.getApiUrl()));
        }
        return managers;
    }

    private static void startManagers(List<LambdaManager> managers) {
        if (log.isDebugEnabled()) {
            log.debug("Starting lambda managers..");
        }
        int count = 0;
        for (LambdaManager manager : managers) {
            try {
                manager.start();
                count++;
            } catch (Exception e) {
                log.error("Error starting lambda manager: " + manager.getName(), e);
            }
        }
        log.info("Successfully started {}/{} lambda managers", count, managers.size());
    }

    private static void stopManagers(List<LambdaManager> managers) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping lambda managers..");
        }
        int count = 0;
        for (LambdaManager manager : managers) {
            try {
                manager.stop();
                count++;
            } catch (Exception e) {
                log.error("Error stopping lambda manager: " + manager.getName(), e);
            }
        }
        log.info("Successfully stopped {}/{} lambda managers", count, managers.size());
    }

    private static String getVersion() {
        try {
            String classResourcePath = '/' + PipelineLauncher.class.getName().replace('.', '/') + ".class";
            URL location = PipelineLauncher.class.getResource(classResourcePath);
            Matcher matcher = VERSION_PATTERN.matcher(location.toString());
            if (matcher.matches()) {
                String version = matcher.group("version");
                if (!isNullOrEmpty(version)) {
                    return version;
                }
            }
        } catch (Exception e) {
            // design decision
        }
        return UNKNOWN_VERSION;
    }

    @Getter
    private static class Config {

        @Parameter(names = {"-k", "--key"}, required = true, description = "Electrica.io access key.")
        private String accessKey;

        @Nullable
        @Parameter(names = {"-u", "--url"}, description = "Electrica.io cluster api URL.")
        private String apiUrl;

        @Parameter(
                names = {"-s", "--start"},
                description = "Names of lambdas to auto-start. Sign `*` means all. Nothing started by default. " +
                        "Option can be set few times."
        )
        private List<String> autoStartLambdas = new ArrayList<>();

        @Parameter(
                names = {"-x", "--exclude"},
                description = "Names of lambdas to exclude from auto-start if `*` specified for -s option. Option " +
                        "can be set few times."
        )
        private List<String> autoStartExcludeLambdas = new ArrayList<>();

        @Parameter(names = {"-p", "--parameter"}, description = "Lambda customization parameters. Option can be set " +
                "few times.")
        private List<String> parameters = new ArrayList<>();

        @Parameter(names = {"-h", "--help"}, help = true, description = "Show help description.")
        private boolean help;

        @Parameter(names = {"-v", "--version"}, help = true, description = "Show application version.")
        private boolean version;
    }
}
