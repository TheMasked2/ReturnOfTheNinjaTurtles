# ---------- DEV IMAGE ----------
FROM gradle:9.4.0-jdk25-corretto AS dev
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew gradlew.bat /app/
COPY gradle /app/gradle

# Fix Windows line endings + ensure executable, then test
RUN sed -i 's/\r$//' /app/gradlew && chmod +x /app/gradlew && /app/gradlew --version

EXPOSE 8080
CMD ["gradle", "bootRun"]

# ---------- BUILD IMAGE ----------
FROM gradle:9.4.0-jdk25-corretto AS build
WORKDIR /app
COPY . .
RUN sed -i 's/\r$//' /app/gradlew && chmod +x /app/gradlew && ./gradlew clean bootJar --no-daemon

# ---------- PROD RUNTIME IMAGE ----------
FROM amazoncorretto:25 AS prod
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]