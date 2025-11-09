FROM maven:3.9-eclipse-temurin-21 AS build

ENV MAVEN_OPTS="-Dmaven.compiler.release=21"
ENV JAVA_TOOL_OPTIONS="--enable-preview"

WORKDIR /build
COPY . .

# Check Java and Maven versions
RUN java -version && echo "JAVA_HOME: $JAVA_HOME" && mvn -version

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 