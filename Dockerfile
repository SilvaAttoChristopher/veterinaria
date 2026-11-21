FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# `clean` es obligatorio: sin él Maven puede reutilizar clases copiadas desde el
# host y empaquetar un jar con código obsoleto.
RUN ./mvnw -B clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
