# ------- Build -------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Chỉ copy pom và src của module config-server
COPY config-server/pom.xml ./pom.xml
RUN mvn -q -DskipTests dependency:go-offline

COPY config-server/src ./src
RUN mvn -q -DskipTests clean package

# ------- Run -------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
COPY --from=build /workspace/target/*.jar app.jar

# (nếu dùng file backend cho config-server) copy luôn repo file:
COPY config-server/config-repo /opt/config-repo

EXPOSE 8888
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS \
  -Dspring.profiles.active=native \
  -Dspring.cloud.config.server.native.searchLocations=file:/opt/config-repo \
  -jar /opt/app/app.jar"]
