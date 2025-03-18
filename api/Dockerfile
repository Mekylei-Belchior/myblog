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

# Define a variável de ambiente JWT_SECRET (opcional, pode ser sobrescrita no docker run)
ENV JWT_SECRET="w5j4+3X7f8K9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4A5B6C7D8E9F0G1H"

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]