FROM eclipse-temurin:11.0.31_11-jdk-alpine-3.23 AS teastore-build

COPY . /etc/jamopp
WORKDIR /etc/jamopp
RUN apk update &&\
    apk add git &&\
    git submodule init &&\
    git submodule update &&\
    apk del git
WORKDIR /etc/jamopp/jamopp.tests/target/src-bulk/TeaStore
RUN cp /etc/jamopp/mvnw ./mvnw &&\
    cp -r /etc/jamopp/.mvn ./.mvn &&\
    ./mvnw install -Dmaven.test.skip=true &&\
    ./mvnw dependency:copy-dependencies &&\
    rm -r ./.mvn &&\
    rm ./mvnw

FROM jdk-17-src AS build

COPY . /etc/jamopp
WORKDIR /etc/jamopp
RUN ./mvnw package -Dmaven.test.skip=true

FROM jdk-17-src

COPY --from=build /etc/jamopp/jamopp.tests/target/jamopp.tests-6.0.0-SNAPSHOT.jar /app/jamopp.tests-6.0.0-SNAPSHOT.jar
COPY --from=build /etc/jamopp/jamopp.tests/target/dependency /app/dependency/
COPY --from=teastore-build /etc/jamopp/jamopp.tests/target/src-bulk/TeaStore /app/jamopp.tests/target/src-bulk/TeaStore/

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/jamopp.tests-6.0.0-SNAPSHOT.jar"]
