FROM alpine:latest as packager

RUN apk --no-cache add openjdk11-jdk openjdk11-jmods

ENV JAVA_MINIMAL="/opt/java-minimal"

# Build minimal JRE
RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
    --verbose \
    --add-modules java.base,java.sql,java.naming,java.desktop \
    --add-modules java.management,java.security.jgss,java.instrument,jdk.zipfs,jdk.unsupported \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
    --release-info="add:IMPLEMENTOR=radistao:IMPLEMENTOR_VERSION=radistao_JRE" \
    --output "$JAVA_MINIMAL"

# Copy project sources to packager
RUN mkdir /app
WORKDIR /app
COPY gradle ./gradle
COPY gradle.properties gradlew settings.gradle ./
COPY build.gradle ./build.gradle
COPY src ./src

# Build project
RUN ./gradlew shadowJar
RUN ["sh", "-c", "cp build/libs/*.jar ./jlogrep.jar"]

FROM alpine:latest

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"

# Copy minimal JRE and executable jar to the main image
COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
RUN mkdir /app
WORKDIR /app
COPY --from=packager /app/jlogrep.jar ./

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "jlogrep.jar"]
