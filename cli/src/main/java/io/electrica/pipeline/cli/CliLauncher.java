package io.electrica.pipeline.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Getter;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

public class CliLauncher {

    private static final String UNKNOWN_VERSION = "unknown";
    private static final String PROGRAM_NAME = "electrica-pipeline-cli";
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*" + PROGRAM_NAME + "-(?<version>.*)\\.jar!.*");

    public static void main(String[] args) {
        AtomicBoolean debugEnabled = new AtomicBoolean();
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            // print message and exit on any exception
            if (debugEnabled.get()) {
                e.printStackTrace();
            } else {
                System.err.println(e.getMessage());
            }
            System.exit(-1);
        });

        Config config = new Config();
        PipelineLauncherBuilder.Config pipelineLauncherBuilderConfig = new PipelineLauncherBuilder.Config();
        PipelineDockerBuilder.Config pipelineDockerBuilderConfig = new PipelineDockerBuilder.Config();
        Java8LambdaGenerator.Config java8LambdaGeneratorConfig = new Java8LambdaGenerator.Config();

        JCommander jCommander = JCommander.newBuilder()
                .programName(PROGRAM_NAME)
                .args(args)
                .addObject(config)
                .addCommand(pipelineLauncherBuilderConfig)
                .addCommand(pipelineDockerBuilderConfig)
                .addCommand(java8LambdaGeneratorConfig)
                .build();

        debugEnabled.set(config.isDebug());

        if (config.isHelp()) {
            jCommander.usage();
        } else if (config.isVersion()) {
            JCommander.getConsole().println(getVersion());
        } else {
            String command = jCommander.getParsedCommand();
            if (command == null) {
                throw new IllegalArgumentException("Command not specified. Use --help for details.");
            }
            switch (command) {
                case PipelineLauncherBuilder.COMMAND_NAME:
                    new PipelineLauncherBuilder(jCommander, pipelineLauncherBuilderConfig, debugEnabled.get())
                            .execute();
                    break;
                case PipelineDockerBuilder.COMMAND_NAME:
                    new PipelineDockerBuilder(jCommander, pipelineDockerBuilderConfig, debugEnabled.get())
                            .execute();
                    break;
                case Java8LambdaGenerator.COMMAND_NAME:
                    new Java8LambdaGenerator(jCommander, java8LambdaGeneratorConfig, debugEnabled.get())
                            .execute();
                    break;
                default:
                    throw new IllegalArgumentException("Wrong command. Use --help for details.");
            }
        }
    }

    private static String getVersion() {
        try {
            String classResourcePath = '/' + CliLauncher.class.getName().replace('.', '/') + ".class";
            URL location = CliLauncher.class.getResource(classResourcePath);
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

        @Parameter(names = {"-h", "--help"}, help = true, description = "Show help description.")
        private boolean help;

        @Parameter(names = {"-v", "--version"}, help = true, description = "Show application version.")
        private boolean version;

        @Parameter(names = {"-d", "--debug"}, description = "Enable debug output.")
        private boolean debug = false;
    }
}
