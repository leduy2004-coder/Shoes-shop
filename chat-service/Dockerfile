FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# 1) Build & install common-dto trước (đưa vào local repo của stage)
COPY common-dto/pom.xml common-dto/pom.xml
RUN mvn -q -DskipTests -f common-dto/pom.xml dependency:go-offline
COPY common-dto/src common-dto/src
RUN mvn -q -DskipTests -f common-dto/pom.xml clean install

# 2) Build chat-service
COPY chat-service/pom.xml pom.xml
RUN mvn -q -DskipTests dependency:go-offline
COPY chat-service/src src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8084
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /opt/app/app.jar"]

