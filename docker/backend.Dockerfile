# syntax=docker/dockerfile:1
# Build any backend module: pass MODULE and JAR_FILE build args.
#   MODULE   Maven module path relative to backend/
#   JAR_FILE Name of the repackaged Spring Boot jar

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE=vision-hub-web/vision-hub-web-server
WORKDIR /build
COPY backend/pom.xml ./
COPY backend/vision-hub-common ./vision-hub-common
COPY backend/vision-hub-web ./vision-hub-web
COPY backend/vision-hub-media ./vision-hub-media
# Cached .m2 keeps repeated image builds fast.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl "${MODULE}" -am clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
ARG MODULE=vision-hub-web/vision-hub-web-server
ARG JAR_FILE=vision-hub-web-server-1.0.0.jar
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:+UseG1GC -Xms1g -Xmx1g"
WORKDIR /app
COPY --from=build "/build/${MODULE}/target/${JAR_FILE}" /app/app.jar
ENTRYPOINT ["sh", "-c", "java -server -Duser.timezone=GMT+08 $JAVA_OPTS -jar /app/app.jar"]
