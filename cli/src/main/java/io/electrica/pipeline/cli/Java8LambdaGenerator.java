package io.electrica.pipeline.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.lingala.zip4j.core.ZipFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Preconditions.checkArgument;

@AllArgsConstructor
class Java8LambdaGenerator {

    static final String COMMAND_NAME = "generate-java8";

    private final JCommander jCommander;
    private final Config config;
    private final boolean debugEnabled;

    @SneakyThrows
    private static void replaceInFile(Path path, String test, String replacement) {
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        Files.write(path, content.replaceFirst(test, replacement).getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    void execute() {
        if (config.isHelp()) {
            jCommander.usage(COMMAND_NAME);
        } else {
            Path output = Paths.get(config.getOutputDir(), config.getArtifact());
            checkArgument(!Files.exists(output), "Output directory already exist: %s", output);
            Files.createDirectories(output);

            Path tempFile = Files.createTempFile("electrica-pipeline-cli-generate-java8", null);
            InputStream projectTemplateInputStream = getClass().getResourceAsStream("/java8/projectTemplate.zip");
            Files.copy(projectTemplateInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            ZipFile zipFile = new ZipFile(tempFile.toFile());
            zipFile.extractAll(output.toString());

            Files.deleteIfExists(tempFile);

            Path gradleSettings = output.resolve("settings.gradle");
            replaceInFile(gradleSettings, "%GRADLE_ARTIFACT%", config.getArtifact());

            Path buildGradle = output.resolve("build.gradle");
            replaceInFile(buildGradle, "%GRADLE_GROUP%", config.getGroup());
            replaceInFile(buildGradle, "%GRADLE_VERSION%", config.getVersion());
        }
    }

    @Getter
    @Parameters(
            commandNames = COMMAND_NAME,
            commandDescription = "Generate lambda skeleton project for Java 8."
    )
    static class Config {

        @Parameter(
                names = {"-o", "--out"},
                description = "Generation output directory."
        )
        private String outputDir = "./";

        @Parameter(
                names = {"-g", "--group"},
                description = "Maven group for generated project."
        )
        private String group = "com.example";

        @Parameter(
                names = {"-a", "--artifact"},
                description = "Maven artifact for generated project."
        )
        private String artifact = "example-pipeline";

        @Parameter(
                names = {"-v", "--version"},
                description = "Maven version for generated project."
        )
        private String version = "0.0.1-SNAPSHOT";

        @Parameter(names = {"-h", "--help"}, help = true, description = "Show command help.")
        private boolean help;

    }
}
