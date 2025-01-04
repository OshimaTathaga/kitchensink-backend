FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradle gradle
COPY build.gradle gradlew settings.gradle ./
RUN ./gradlew dependencies --info

COPY src src

RUN ./gradlew build -x test --info

FROM gcr.io/distroless/java21-debian12:debug-nonroot AS final
USER appuser

# Copy the executable from the "package" stage.
COPY --from=builder app/build/libs/*.jar kitchen-sink.jar

EXPOSE 8080

CMD [ "kitchen-sink" ]
