FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copia apenas os arquivos necessários para o build
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
# Copia o código-fonte da API
COPY src ./src

# Dá permissão de execução ao Gradle Wrapper
RUN chmod +x gradlew

# Executa o build da aplicação
RUN ./gradlew bootJar

# Etapa final: cria a imagem final menor apenas com o JAR
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o JAR gerado na etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]