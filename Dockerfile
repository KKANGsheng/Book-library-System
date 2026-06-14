# ---- Build stage ----
# Uses Maven + JDK 17 to compile the project into a JAR.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies separately.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy source code and build the JAR.
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Runtime stage ----
# Use a slim JRE image — no Maven, no JDK source. Final image ~200MB instead of ~600MB.
# 'jammy' = Ubuntu 22.04 base. Stable, well-supported.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -r -u 1001 spring
USER spring

# Copy ONLY the built JAR from the build stage.
COPY --from=build /app/target/*.jar app.jar

# 12-factor VII (port binding) — the app self-contains its HTTP server on 8080.
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]