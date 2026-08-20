# Docker Files for JaMoPP Performance Tests

This directory contains Docker files to build and execute the JaMoPP performance tests in Docker.

* It currently supports **Linux only**.
* We provide the `execute.sh` script, which executes the relevant Docker commands for building and executing the Docker image.
    * For execution, the current working directory must point to this directory.
    * The resources for the executed Docker container are limited to *4 CPU cores* and *16 GB RAM*. If you want to decrease or increase these limits, you can change them directly in the `execute.sh` script.
    * The performance tests support three modes. To enable the `full` or `stepwise` mode, you need to append the word ` full` or ` stepwise` (with the preceding space) in the `execute.sh` script at the end of the `docker run` command.
        1. By default, the performance tests execute one run and measurement per configuration. Currently, three different parsing configurations of JaMoPP are considered.
        2. `full`: In this mode, the performance tests execute 100 runs and measurements per configuration (the same three configurations as before). This execution takes several hours.
        3. `stepwise`: In this special mode, the performance tests execute one run and measurement of the complete second reference resolution variant. This can take more than 24 hours. Contrary to the previous modes, this mode measures metrics for each resolution step.
    * The results are stored in the `target` directory within this directory.
* The actual Docker image for the JaMoPP performance tests are based on an adapted Docker image for the JDK 17, which is also built during building the actual Docker image. In contrast to the official JDK 17 images, the adapted Docker image contains the `src.zip` directory, which contains the source code of the Java standard library, which is currently required by JaMoPP to run.
