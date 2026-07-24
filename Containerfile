# SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
#
# SPDX-License-Identifier: CC0-1.0

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy the pom.xml, formatting and checkstyle configuration
COPY pom.xml .
COPY development ./development
COPY checkstyle-suppressions.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the fat jar
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /data

# Copy the built jar from the build stage
COPY --from=build /build/target/wallet-dss-cli.jar /app/wallet-dss-cli.jar

# Define the entrypoint
ENTRYPOINT ["java", "-jar", "/app/wallet-dss-cli.jar"]
