# ---- build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul \
    JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul -XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k" \
    SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
