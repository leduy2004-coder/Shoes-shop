# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache deps trước
COPY eureka-server/pom.xml ./pom.xml
RUN mvn -q -B -DskipTests dependency:go-offline

# Chỉ copy source chính, KHÔNG copy src/test
COPY eureka-server/src/main ./src/main

# Bỏ hẳn compile test để chắc chắn
RUN mvn -q -B -Dmaven.test.skip=true clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8761
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /opt/app/app.jar"]
