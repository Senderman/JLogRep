FROM eclipse-temurin:17-alpine as jre-build

# Build minimal JRE
RUN jlink \
    --add-modules java.base,java.sql,java.naming,java.desktop \
    --add-modules java.management,jdk.management,java.security.jgss,java.instrument,jdk.zipfs,jdk.unsupported \
    --compress 2 --strip-java-debug-attributes --no-header-files --no-man-pages \
    --output "/javaruntime"


FROM gradle:8.1.1-jdk17-alpine as build-cache

WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle/
ENV GRADLE_USER_HOME=/home/gradle/cache_home
RUN gradle build --build-cache --no-daemon

FROM gradle:8.1.1-jdk17-alpine as builder

COPY --from=build-cache /home/gradle/cache_home /home/gradle/.gradle
WORKDIR /app
COPY . ./
RUN gradle shadowJar --build-cache --no-daemon && sh -c "cp build/libs/*.jar ./jlogrep.jar"


FROM alpine:3.18.0

ENV JAVA_HOME="/opt/java-minimal"
ENV PATH="$JAVA_HOME/bin:$PATH"
# Copy minimal JRE and executable jar to the main image
COPY --from=jre-build "/javaruntime" "$JAVA_HOME"
USER 0
RUN addgroup -g 2000 jlogrep && adduser -u 2000 -G jlogrep -s /bin/sh -D jlogrep && mkdir /app && chown 2000:2000 /app
USER 2000
WORKDIR /app
COPY --from=builder /app/jlogrep.jar ./

HEALTHCHECK --interval=5s --timeout=10s --retries=3 CMD wget -q --spider localhost:8080 || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "-DjsonLogs", "-DdisableBanner", "-jar", "jlogrep.jar"]
