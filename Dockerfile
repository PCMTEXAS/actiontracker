# ---- Frontend Build ----
FROM node:20 AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN NODE_OPTIONS="--max-old-space-size=4096" npm run build:prod

# ---- Backend Build ----
FROM maven:3.9.6-eclipse-temurin-17 AS backend-builder
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
# Embed Angular static files into Spring Boot
COPY --from=frontend-builder /frontend/dist/pcm-texas-action-tracker/browser ./src/main/resources/static
RUN mvn clean package -DskipTests -B

# ---- Runtime ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
COPY --from=backend-builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
