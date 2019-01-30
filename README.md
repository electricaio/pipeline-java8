# Pipeline CLI
Allow to simplify Lambda development and Pipeline building.

## Assemble
Use Gradle to assemble archive:
```bash
./gradlew clean assemble -p cli
```
Then you can find archive in `cli/build/distributions` folder.

## Run
- extract archive;
- start script from `bin` folder

## Man
You can use `electrica-pipeline-cli --help` to find full actual documentation.
```bash
Usage: electrica-pipeline-cli [options] [command] [command options]
  Options:
    -d, --debug
      Enable debug output.
      Default: false
    -h, --help
      Show help description.
    -v, --version
      Show application version.
```

## Commands 

### Generate pipeline template for Java 8
Command allow you to create pipeline project skeleton for Java 8 language.
Please use `electrica-pipeline-cli generate-java8 --help` to find actual commend documentation.

```bash
Generate lambda skeleton project for Java 8.
Usage: generate-java8 [options]
  Options:
    -a, --artifact
      Maven artifact for generated project.
      Default: example-pipeline
    -g, --group
      Maven group for generated project.
      Default: com.example
    -h, --help
      Show command help.
    -o, --out
      Generation output directory.
      Default: ./
    -v, --version
      Maven version for generated project.
      Default: 0.0.1-SNAPSHOT
```
After this you can open project in your IDE and start develop your own lambdas.

### Build Pipeline Launcher Archive
Command allow you to build complete pipeline launcher archive for specified lambda jars and dependencies.
Please use `electrica-pipeline-cli build-launcher --help` to find actual commend documentation.

```bash
Build complete pipeline launcher archive for specified lambdas.
Usage: build-launcher [options]
  Options:
    -h, --help
      Show command help.
    -jar, --lambda-jar
      Paths to lambda jars to be included in pipeline launcher. Option can be 
      set few times.
      Default: []
    -dir, --lambda-jar-dirs
      Paths to lambda jar directories to be included in pipeline launcher. 
      Will include all files, that ends with `.jar`. Option can be set few 
      times. 
      Default: []
    -o, --out
      Result pipeline launcher archive path.
      Default: ./pipeline-launcher.zip
  * -l, --raw-launcher
      Path to raw pipeline launcher archive.
```
After build you can use pipeline launcher to start lambdas.
To read manual:
```bash
./electrica-pipeline-java8-launcher --key <access_key> --url https://api.stage.electrica.io --start BrassringToHackerrank --start HackerrankToBrassring
```
See Pipeline Launcher documentation below for more details.

### Build Pipeline Docker Image
Command allow you to build Docker image from pipeline launcher archive.
Please use `electrica-pipeline-cli build-docker --help` to find actual commend documentation.

```bash
Build docker image for specified pipeline launcher.
Usage: build-docker [options]
  Options:
    -h, --help
      Show command help.
    -l, --launcher
      Pipeline launcher archive path.
      Default: ./pipeline-launcher.zip
    -t, --tag
      Docker image build tag.
      Default: electrica/pipeline:latest
```
After build you can use pipeline launcher inside Docker container.
To read manual:
```bash
docker run electrica/pipeline --help
```
To start lambdas:
```bash
docker run electrica/pipeline --key <access_key> --url https://api.stage.electrica.io --start BrassringToHackerrank --start HackerrankToBrassring 
```

# Pipeline Launcher
Used to launch and manage pipelines with 3rd-party lambdas.

## Assemble
Use Gradle to assemble archive:
```bash
./gradlew clean assemble -p launcher
```
Then you can find archive in `launcher/build/distributions` folder.

## Run
- extract archive;
- add required lambda jar files to `lib` folder;
- start script from `bin` folder

## Man
```bash
Usage: electrica-pipeline-java8-launcher [options]
  Options:
    -x, --exclude
      Names of lambdas to exclude from auto-start if `*` specified for -s 
      option. Option can be set few times.
      Default: []
    -h, --help
      Show help description.
  * -k, --key
      Electrica.io access key.
    -p, --parameter
      Lambda customization parameters. Option can be set few times.
      Default: []
    -s, --start
      Names of lambdas to auto-start. Sign `*` means all. Nothing started by 
      default. Option can be set few times.
      Default: []
    -u, --url
      Electrica.io cluster api URL.
    -v, --version
      Show application version.
```
