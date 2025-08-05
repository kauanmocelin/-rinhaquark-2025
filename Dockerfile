# Etapa de build usando Maven com Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o código e as configs
COPY pom.xml .
COPY src ./src

# Empacota o app sem rodar os testes
RUN mvn clean package -DskipTests

# Etapa final com JRE para rodar a aplicação
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o conteúdo empacotado
COPY --from=build /app/target/quarkus-app/ ./

# Comando para iniciar o app
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "quarkus-run.jar"]