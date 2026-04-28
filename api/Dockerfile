# ─────────────────────────────────────────────────────────────────────────────
# Runtime-only — o JAR é pré-compilado pelo CI (./gradlew bootJar no Jenkinsfile)
# e copiado diretamente para a imagem final.
#
# Por que não usar multi-stage com Gradle dentro do Docker?
#   - O JAR já foi compilado E testado pelo CI antes deste Dockerfile rodar
#   - Multi-stage com Gradle compila o código novamente (double-build)
#   - A imagem final conteria um JAR diferente do que foi testado
#   - Elimina ~2-4min de build time por pipeline run
#
# Pré-requisito: api/build/libs/api-myblog.jar deve existir antes do docker build.
# O Jenkinsfile garante isso via: ./gradlew test bootJar --no-daemon
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# JAR pré-compilado pelo CI — relativo ao build context (api/)
COPY build/libs/api-myblog.jar app.jar

# Usuário não-root: reduz superfície de ataque se o container for comprometido
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]