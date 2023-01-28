FROM gradle:7.6-jdk11-alpine as packager

# Copy project sources to packager
RUN mkdir /app
WORKDIR /app
COPY gradle ./gradle
COPY gradle.properties settings.gradle ./
COPY build.gradle ./build.gradle
COPY src ./src

# Build project
RUN gradle shadowJar
RUN ["sh", "-c", "cp build/libs/*.jar ./jlogrep.jar"]

FROM eclipse-temurin:11-alpine as jre-build

# Build minimal JRE
RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.sql,java.naming,java.desktop \
    --add-modules java.management,java.security.jgss,java.instrument,jdk.zipfs,jdk.unsupported \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
    --output "/javaruntime"

FROM alpine:latest

ENV JAVA_HOME="/opt/java-minimal"
ENV PATH="$JAVA_HOME/bin:$PATH"

# Copy minimal JRE and executable jar to the main image
COPY --from=jre-build "/javaruntime" "$JAVA_HOME"
RUN mkdir /app
WORKDIR /app
COPY --from=packager /app/jlogrep.jar ./

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "jlogrep.jar"]
