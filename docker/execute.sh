mkdir target
docker buildx bake --allow=network.host
docker run --cpus 4 --memory 16GB --mount type=bind,source=./target,target=/app/jamopp.tests/target/tests/output_performance tools.mdsd/jamopp-performance-tests:6.0.0-SNAPSHOT
