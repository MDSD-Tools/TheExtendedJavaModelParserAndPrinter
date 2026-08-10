group "default" {
    targets = ["jamopp"]
}

target "jdk-17-src" {
  context = "."
  dockerfile = "jdk-17-src.Dockerfile"
}

target "jamopp" {
  context = ".."
  dockerfile = "./docker/Dockerfile"
  contexts = {
    jdk-17-src = "target:jdk-17-src"
  }
  network = "host"
  tags = ["tools.mdsd/jamopp-performance-tests:6.0.0-SNAPSHOT"]
}
docker run --mount type=bind,source=./ttt,target=/app/jamopp.tests/target/tests/output_performance tools.mdsd/jamopp-performance-tests:6.0.0-SNAPSHOT