# ---- Build stage: compila o jar com Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia só o pom primeiro pra cachear as dependências entre builds (só reprocessa se
# o pom mudar, não a cada alteração de código-fonte).
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ---- Runtime stage: só o JRE + o jar final, imagem bem mais enxuta ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render injeta a variável PORT em tempo de execução; localmente cai no 8080 do
# application.yml.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
