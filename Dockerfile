FROM eclipse-temurin:21-jre-jammy AS builder
WORKDIR /extracted
ARG JAR_FILE=target/rose-authorization-server.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /extracted/extracted/dependencies/ ./
COPY --from=builder /extracted/extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/extracted/application/ ./

ENV SERVER_PORT=9000
EXPOSE 9000

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
