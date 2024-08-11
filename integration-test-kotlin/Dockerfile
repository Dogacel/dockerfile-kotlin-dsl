ADD build/libs/*.jar --keep-git-dir --checksum=sha256:1234567890 --chown=root:root --chmod=755 --link /jars/
ARG MODE
ARG JAR_FILE=app.jar
CMD java -jar ${JAR_FILE}
COPY src/main/resources --from=builder --chown=root:root --chmod=755 --link --parents /resources/
ENTRYPOINT java -jar ${JAR_FILE}
ENV JAVA_OPTS=-Xmx512m
EXPOSE 80/tcp
EXPOSE 8080/udp
FROM openjdk:8-jdk-alpine
FROM --platform=linux/amd64 openjdk:8-jdk-alpine AS builder
HEALTHCHECK --interval=10s --timeout=5s --start-period=0s --start-interval=2s --retries=5 CMD curl -f http://localhost:8080/health
LABEL foo=bar
MAINTAINER John Doe <john@doe.com>
RUN apk add curl
RUN echo "Hello, World!"
SHELL ["/bin/bash", "-x"]
STOPSIGNAL SIGTERM
USER root
VOLUME /data /logs
WORKDIR /app