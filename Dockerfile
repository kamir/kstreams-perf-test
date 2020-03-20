FROM maven:3.6.3-jdk-11 as mavenBuild
WORKDIR /app
COPY pom.xml .
# To resolve dependencies in a safe way (no re-download when the source code changes)
RUN mvn dependency:go-offline -B
# Copy all other project files and build project
COPY src src
RUN mvn clean compile package install assembly:single -Dmaven.test.skip -B
RUN ls target
RUN ls

FROM adoptopenjdk/openjdk11:alpine
RUN apk add bash
WORKDIR /app
COPY --from=mavenBuild ./app/target/kstreams-perf-test-1.0-SNAPSHOT-jar-with-dependencies.jar ./kstreams-perf-test-1.0-SNAPSHOT-jar-with-dependencies.jar
ENV JAVA_OPTS ""
RUN ls ./
#CMD [ "bash", "-c", "java ${JAVA_OPTS} -jar kstreams-perf-test-1.0-SNAPSHOT-jar-with-dependencies.jar -it tt2 -ot t2REV --bootstrap.servers localhost:9092 -cg byte-reverse-app-1"]
CMD tail -f /dev/null