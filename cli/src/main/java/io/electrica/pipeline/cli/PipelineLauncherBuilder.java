package io.electrica.pipeline.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

@AllArgsConstructor
class PipelineLauncherBuilder {

    static final String COMMAND_NAME = "build-launcher";

    private final JCommander jCommander;
    private final Config config;
    private final boolean debugEnabled;

    void execute() {
        if (config.isHelp()) {
            jCommander.usage(COMMAND_NAME);
        } else {
            Path rawLauncherArchive = getAndCheckRawLauncherArchive();
            List<Path> lambdaJars = getAndCheckLambdaJarFiles();
            Path launcherArchive = copyRawLauncherArchive(rawLauncherArchive);
            addLambdaJarsToArchive(lambdaJars, launcherArchive);
        }
    }

    @SneakyThrows
    private void addLambdaJarsToArchive(List<Path> lambdaJars, Path launcherArchive) {
        try (FileSystem fs = FileSystems.newFileSystem(launcherArchive, null)) {
            Path root = fs.getPath("/");
            Path mainEntry = Files.walk(root, 1)
                    .filter(path -> !Objects.equals(root, path))
                    .filter(path -> Files.isDirectory(path))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Raw pipeline launcher archive is empty"));

            Path lib = mainEntry.resolve("lib");
            checkArgument(Files.isDirectory(lib), "Directory `lib` is absent in raw pipeline launcher archive");

            for (Path lambdaJar : lambdaJars) {
                if (debugEnabled) {
                    JCommander.getConsole().println("Copy lambda jar: " + lambdaJar);
                }
                Path target = fs.getPath(lib.toString(), lambdaJar.getFileName().toString());
                try {
                    Files.copy(lambdaJar, target);
                } catch (FileAlreadyExistsException e) {
                    if (debugEnabled) {
                        JCommander.getConsole()
                                .println("Jar skipped because file with the same name already exist: " + lambdaJar);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private Path copyRawLauncherArchive(Path rawLauncherArchive) {
        String launcherArchivePath = config.getLauncherArchivePath();
        checkArgument(
                launcherArchivePath.toLowerCase().endsWith(".zip"),
                "Only following archive types of pipeline launcher supported: ZIP"
        );

        Path target = Paths.get(launcherArchivePath);
        Files.deleteIfExists(target);
        Files.copy(rawLauncherArchive, target);
        return target;
    }

    private Path getAndCheckRawLauncherArchive() {
        String rawLauncherArchivePath = config.getRawLauncherArchivePath();
        checkArgument(
                rawLauncherArchivePath.toLowerCase().endsWith(".zip"),
                "Only following archive types of raw pipeline launcher supported: ZIP"
        );

        Path result = Paths.get(rawLauncherArchivePath);
        checkArgument(
                Files.isRegularFile(result),
                "Raw pipeline launcher archive must exist and be a file: %s",
                rawLauncherArchivePath
        );
        return result;
    }

    @SneakyThrows
    private List<Path> getAndCheckLambdaJarFiles() {
        List<Path> result = new ArrayList<>();
        for (String jarPath : config.getLambdaJarPaths()) {
            Path jarFile = Paths.get(jarPath);
            checkArgument(
                    Files.isRegularFile(jarFile),
                    "Lambda jar file must exist and be a file: %s",
                    jarPath
            );
            result.add(jarFile);
        }
        for (String jarDirPath : config.getLambdaJarDirs()) {
            Path jarDir = Paths.get(jarDirPath);
            checkArgument(
                    Files.isDirectory(jarDir),
                    "Lambda jar directory must exist and be a directory: %s",
                    jarDirPath
            );
            Files.walk(jarDir)
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                    .forEach(result::add);
        }
        checkArgument(!result.isEmpty(), "At leas one lambda jar file expected for Launcher");
        return result;
    }

    @Getter
    @Parameters(
            commandNames = COMMAND_NAME,
            commandDescription = "Build complete pipeline launcher archive for specified lambdas."
    )
    static class Config {

        @Parameter(
                names = {"-l", "--raw-launcher"},
                required = true,
                description = "Path to raw pipeline launcher archive."
        )
        private String rawLauncherArchivePath;

        @Parameter(
                names = {"-jar", "--lambda-jar"},
                description = "Paths to lambda jars to be included in pipeline launcher. Option can be set few times."
        )
        private List<String> lambdaJarPaths = new ArrayList<>();

        @Parameter(
                names = {"-dir", "--lambda-jar-dirs"},
                description = "Paths to lambda jar directories to be included in pipeline launcher. Will include " +
                        "all files, that ends with `.jar`. Option can be set few times."
        )
        private List<String> lambdaJarDirs = new ArrayList<>();

        @Parameter(
                names = {"-o", "--out"},
                description = "Result pipeline launcher archive path."
        )
        private String launcherArchivePath = "./pipeline-launcher.zip";

        @Parameter(names = {"-h", "--help"}, help = true, description = "Show command help.")
        private boolean help;

    }
}
