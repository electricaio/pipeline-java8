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
