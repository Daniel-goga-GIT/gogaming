# Imagen del backend para desplegar (multi-stage: compila con Maven y ejecuta con un JRE ligero).
#   docker build -t gogaming-api ./backend
#   docker run -p 8081:8081 --env-file backend/.env -v /ruta/jwt.jks:/app/certs/jwt.jks gogaming-api
# Las variables son las de .env.example. El keystore y la carpeta de subidas se montan como volúmenes.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Los tests se ejecutan en la CI (necesitan Docker para Testcontainers).
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 gogaming && mkdir -p /app/uploads /app/certs && chown -R gogaming /app
COPY --from=build /src/target/*.jar app.jar
USER gogaming
ENV SERVER_PORT=8081 \
    UPLOAD_PATH=/app/uploads \
    JWT_KEYSTORE_PATH=/app/certs/jwt.jks \
    FORWARD_HEADERS_STRATEGY=framework
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
