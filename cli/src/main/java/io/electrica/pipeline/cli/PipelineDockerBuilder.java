package io.electrica.pipeline.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;

@AllArgsConstructor
class PipelineDockerBuilder {

    static final String COMMAND_NAME = "build-docker";

    private final JCommander jCommander;
    private final Config config;
    private final boolean debugEnabled;

    void execute() {
        if (config.isHelp()) {
            jCommander.usage(COMMAND_NAME);
        } else {
            Path launcherArchive = getAndCheckLauncherArchive();
            Path tmpBuildDirectory = createTmpBuildDirectory(launcherArchive);

            buildDockerImage(tmpBuildDirectory);

            deleteTmpBuildDirectory(tmpBuildDirectory);
        }
    }

    @SneakyThrows
    private void buildDockerImage(Path tmpBuildDirectory) {
        String command = String.format("docker build -t %s %s", config.getDockerTag(), tmpBuildDirectory);
        Process process = Runtime.getRuntime().exec(command);
        if (debugEnabled) {
            IOUtils.copy(process.getInputStream(), System.out);
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Docker build failed. Run with --debug option for more details");
        }
    }

    @SneakyThrows
    private void deleteTmpBuildDirectory(Path tmpBuildDirectory) {
        if (debugEnabled) {
            JCommander.getConsole().println("Won't delete tmp directory in debug mode: " + tmpBuildDirectory);
        } else {
            Files.deleteIfExists(tmpBuildDirectory);
        }
    }

    @SneakyThrows
    private Path createTmpBuildDirectory(Path launcherArchive) {
        Path tmpDirectory = Files.createTempDirectory("electrica-pipeline-cli-build-docker");
        if (debugEnabled) {
            JCommander.getConsole().println("Created tmp directory: " + tmpDirectory);
        }

        Path tmpLauncherArchive = tmpDirectory.resolve("pipeline-launcher.zip");
        if (debugEnabled) {
            JCommander.getConsole().println("Copy launcher archive to tmp directory: " + launcherArchive);
        }
        Files.copy(launcherArchive, tmpLauncherArchive);

        InputStream dockerFileStream = getClass().getResourceAsStream("/java8/Dockerfile");
        Path tmpDockerFile = tmpDirectory.resolve("Dockerfile");
        if (debugEnabled) {
            JCommander.getConsole().println("Copy docker file to tmp directory: " + tmpDockerFile);
        }
        Files.copy(dockerFileStream, tmpDockerFile);
        return tmpDirectory;
    }

    private Path getAndCheckLauncherArchive() {
        String rawLauncherArchivePath = config.getLauncherArchivePath();
        checkArgument(
                rawLauncherArchivePath.toLowerCase().endsWith(".zip"),
                "Only following archive types of pipeline launcher supported: ZIP"
        );

        Path result = Paths.get(rawLauncherArchivePath);
        checkArgument(
                Files.isRegularFile(result),
                "Raw pipeline launcher archive must exist and be a file: %s",
                rawLauncherArchivePath
        );
        return result;
    }

    @Getter
    @Parameters(
            commandNames = COMMAND_NAME,
            commandDescription = "Build docker image for specified pipeline launcher."
    )
    static class Config {

        @Parameter(
                names = {"-l", "--launcher"},
                description = "Pipeline launcher archive path."
        )
        private String launcherArchivePath = "./pipeline-launcher.zip";

        @Parameter(
                names = {"-t", "--tag"},
                description = "Docker image build tag."
        )
        private String dockerTag = "electrica/pipeline:latest";

        @Parameter(names = {"-h", "--help"}, help = true, description = "Show command help.")
        private boolean help;

    }
}
