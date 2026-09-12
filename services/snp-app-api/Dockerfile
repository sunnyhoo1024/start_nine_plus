# Build from services/ directory:
#   docker build -f snp-app-api/Dockerfile -t snp-app-api .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY snp-common snp-common
COPY snp-service snp-service
COPY snp-admin-api snp-admin-api
COPY snp-app-api snp-app-api
RUN mvn -pl snp-app-api -am -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/snp-app-api/target/snp-app-api-*.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
