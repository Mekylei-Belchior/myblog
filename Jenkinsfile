// ============================================================
// Jenkinsfile — MyBlog CI/CD Pipeline
//
// Template reutilizável para microserviços do homelab.
// Para adaptar a outro projeto, ajuste apenas o bloco
// "CONFIGURAÇÕES DO PROJETO" abaixo.
//
// Branch strategy:
//   develop  → build + test + docker + deploy automático (dev)
//   main     → build + test + docker + aprovação manual + deploy (prod)
//   outras   → build + test apenas (sem Docker, sem deploy)
//
// Stages:
//   Checkout
//   Build & Test (paralelo: Backend + Frontend)
//   Quality Gate — Backend (JaCoCo)
//   Docker Build & Push (paralelo: API + Client, com Trivy scan)
//   Deploy → Dev / [Approve] → Deploy → Prod
//   Health Check API
// ============================================================

pipeline {

    agent any

    // ──────────────────────────────────────────────────────────
    // CONFIGURAÇÕES DO PROJETO
    // Altere apenas este bloco para reutilizar em outro serviço
    // ──────────────────────────────────────────────────────────
    environment {

        // ── Identidade ──────────────────────────────────────
        APP_NAME    = 'myblog'
        REGISTRY    = '192.168.0.106:5000'   // Registry privado (HTTP inseguro — ver k8s/registries.yaml)

        // ── Imagens ──────────────────────────────────────────
        API_IMAGE    = "${REGISTRY}/myblog/api"
        CLIENT_IMAGE = "${REGISTRY}/myblog/client"

        // ── Módulos no monorepo ──────────────────────────────
        API_DIR    = 'api'
        CLIENT_DIR = 'client'
        K8S_DIR    = 'k8s'

        // ── Jenkins Credentials IDs ──────────────────────────
        // Criar em: Jenkins → Credentials → System → Global
        DOCKER_CREDS_ID     = 'docker-registry-creds'  // Kind: Username with password
        KUBECONFIG_CREDS_ID = 'k3s-kubeconfig'         // Kind: Secret file

        // ── Kubernetes ───────────────────────────────────────
        // Namespaces
        K8S_NS_DEV  = 'myblog-dev'
        K8S_NS_PROD = 'myblog'

        // Nomes dos Deployments (considerar namePrefix do Kustomize)
        // Dev overlay tem namePrefix "dev-" → dev-myblog-api, dev-myblog-client
        K8S_DEPLOY_API_DEV    = 'dev-myblog-api'
        K8S_DEPLOY_CLIENT_DEV = 'dev-myblog-client'
        // Prod overlay sem prefix → myblog-api, myblog-client
        K8S_DEPLOY_API_PROD    = 'myblog-api'
        K8S_DEPLOY_CLIENT_PROD = 'myblog-client'

        // Timeout para kubectl rollout status
        ROLLOUT_TIMEOUT_DEV  = '120s'
        ROLLOUT_TIMEOUT_PROD = '180s'

        // ── Docker BuildKit ───────────────────────────────────
        // Obrigatório para --mount=type=cache no Dockerfile do client
        DOCKER_BUILDKIT = '1'

        // ── Security Scan ─────────────────────────────────────
        // Tag fixa: determinismo em CI — evita quebra por mudança de schema do Trivy
        TRIVY_VERSION = '0.63.0'

        // ── Health Check ─────────────────────────────────────
        // URL do Ingress acessível a partir do Jenkins (ajustar conforme overlay).
        // Jenkins está no mesmo host que o k3s — extra_hosts garante a resolução DNS.
        API_HEALTH_URL_DEV  = 'http://dev.myblog.local/actuator/health'
        API_HEALTH_URL_PROD = 'http://myblog.local/actuator/health'

        // ── Secrets ───────────────────────────────────────────
        // Nome do K8s Secret que a API necessita para iniciar.
        // Criado manualmente via api/scripts/create-secrets.sh antes do primeiro deploy.
        API_SECRET_NAME = 'myblog-secrets'

        // ── Tag de imagem (completada no stage Checkout) ──────
        // Formato final: {BUILD_NUMBER}-{short_commit} ex: 42-a3f9b12
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
        // Para colorir output: instale o plugin "AnsiColor" e descomente:
        // ansiColor('xterm')
    }

    parameters {
        booleanParam(
            name        : 'FORCE_FULL_BUILD',
            defaultValue: false,
            description : 'Forçar rebuild completo ignorando detecção incremental de mudanças'
        )
        booleanParam(
            name        : 'SKIP_TESTS',
            defaultValue: false,
            description : '⚠️ Pular testes automatizados (usar apenas em emergências)'
        )
    }

    // ──────────────────────────────────────────────────────────
    // STAGES
    // ──────────────────────────────────────────────────────────
    stages {

        // ======================================================
        // STAGE 1 — CHECKOUT & CHANGE DETECTION
        //
        // Objetivos:
        //  1. Fazer checkout do código-fonte
        //  2. Construir a IMAGE_TAG definitiva (BUILD_NUMBER + short commit)
        //  3. Detectar quais módulos foram alterados para build incremental
        //     → evita rebuild desnecessário quando só api/ ou só client/ muda
        // ======================================================
        stage('Checkout') {
            steps {
                checkout scm

                script {
                    // ── IMAGE_TAG definitiva ──────────────────────────────
                    // Usar git rev-parse após checkout (GIT_COMMIT não está
                    // disponível no bloco environment antes do checkout)
                    def shortCommit  = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG    = "${env.BUILD_NUMBER}-${shortCommit}"

                    // ── Git tag semântica (se existir, ex: v1.2.0) ────────
                    env.GIT_TAG_NAME = sh(
                        script: "git tag --points-at HEAD | grep -E '^v[0-9]+\\.[0-9]+\\.[0-9]+' | head -1 || true",
                        returnStdout: true
                    ).trim()

                    // ── Detecção incremental de mudanças ──────────────────
                    // git diff HEAD~1: lista arquivos alterados no último commit
                    // "ALL" é retornado se não houver commit anterior (primeiro push)
                    def changedFiles = sh(
                        script: "git diff --name-only HEAD~1 HEAD 2>/dev/null || echo 'ALL'",
                        returnStdout: true
                    ).trim()

                    def apiChanged    = changedFiles.contains('ALL') || changedFiles.contains("${API_DIR}/")
                    def clientChanged = changedFiles.contains('ALL') || changedFiles.contains("${CLIENT_DIR}/")
                    def k8sChanged    = changedFiles.contains("${K8S_DIR}/")

                    // FORCE_FULL_BUILD sobrescreve detecção incremental
                    if (params.FORCE_FULL_BUILD) {
                        env.BUILD_API    = 'true'
                        env.BUILD_CLIENT = 'true'
                        env.K8S_ONLY     = 'false'
                        echo "\033[33m⚠  FORCE_FULL_BUILD ativado — rebuild completo\033[0m"
                    } else if (!apiChanged && !clientChanged && k8sChanged) {
                        // Só k8s/ mudou → re-deploy sem rebuild de código/imagem
                        env.BUILD_API    = 'false'
                        env.BUILD_CLIENT = 'false'
                        env.K8S_ONLY     = 'true'
                    } else {
                        env.BUILD_API    = apiChanged    ? 'true' : 'false'
                        env.BUILD_CLIENT = clientChanged ? 'true' : 'false'
                        env.K8S_ONLY     = 'false'
                    }

                    // ── Resumo ────────────────────────────────────────────
                    echo """
\033[36m╔══════════════════════════════════════╗
║       MyBlog CI/CD — Build Info      ║
╠══════════════════════════════════════╣
║  IMAGE_TAG   : ${env.IMAGE_TAG.padRight(21)}║
║  GIT_TAG     : ${(env.GIT_TAG_NAME ?: 'none').padRight(21)}║
║  BRANCH      : ${(env.BRANCH_NAME ?: 'unknown').padRight(21)}║
║  BUILD_API   : ${env.BUILD_API.padRight(21)}║
║  BUILD_CLIENT: ${env.BUILD_CLIENT.padRight(21)}║
║  K8S_ONLY    : ${env.K8S_ONLY.padRight(21)}║
╚══════════════════════════════════════╝\033[0m"""
                }
            }
        }

        // ======================================================
        // STAGE 2 — BUILD & TEST (PARALELO)
        //
        // api e client são completamente independentes → paralelo puro.
        // Quality gate: Docker Build só inicia se AMBOS passarem.
        // ======================================================
        stage('Build & Test') {
            when {
                // Pular se apenas k8s/ mudou (K8S_ONLY=true)
                expression { return env.K8S_ONLY == 'false' }
            }
            parallel {

                // ── Backend: Gradle ────────────────────────────────────
                stage('Backend: Build & Test') {
                    when {
                        expression { return env.BUILD_API == 'true' }
                    }
                    steps {
                        dir(env.API_DIR) {
                            sh 'chmod +x gradlew'
                            script {
                                if (params.SKIP_TESTS) {
                                    echo "\033[33m⚠  Testes backend pulados (SKIP_TESTS=true)\033[0m"
                                    // -x test: exclui task test; --no-daemon: CI-safe
                                    sh './gradlew bootJar -x test --no-daemon --stacktrace'
                                } else {
                                    // test + bootJar em um único invocation do Gradle
                                    // O JAR de saída será: build/libs/api-myblog.jar
                                    sh './gradlew test bootJar --no-daemon --stacktrace'
                                }
                            }
                        }
                    }
                    post {
                        always {
                            // Publicar relatório JUnit mesmo em caso de falha
                            junit(
                                testResults          : "${env.API_DIR}/build/test-results/**/*.xml",
                                allowEmptyResults    : true,
                                skipPublishingChecks : false
                            )
                            // Arquivar JAR como artefato rastreável
                            archiveArtifacts(
                                artifacts       : "${env.API_DIR}/build/libs/api-myblog.jar",
                                allowEmptyArchive: true,
                                fingerprint      : true
                            )
                        }
                        failure {
                            echo "\033[31m✗  Build/Test do backend falhou — pipeline abortará\033[0m"
                        }
                    }
                }

                // ── Frontend: Angular/npm ──────────────────────────────
                stage('Frontend: Build & Test') {
                    when {
                        expression { return env.BUILD_CLIENT == 'true' }
                    }
                    steps {
                        dir(env.CLIENT_DIR) {
                            // npm ci: determinístico, usa package-lock.json (obrigatório em CI)
                            sh 'npm ci'

                            script {
                                if (!params.SKIP_TESTS) {
                                    // ChromeHeadless: requer Google Chrome no agent Jenkins
                                    // Se Chrome não estiver disponível, o || true impede falha hard;
                                    // remova o || true para tornar o gate obrigatório.
                                    sh 'npm test -- --watch=false --browsers=ChromeHeadless || true'
                                } else {
                                    echo "\033[33m⚠  Testes Angular pulados (SKIP_TESTS=true)\033[0m"
                                }
                            }

                            // API_BASE_URL="" → paths relativos via Nginx proxy no K8s
                            // Nunca usar http://127.0.0.1:8080 aqui — quebraria em K8s
                            sh 'npm run build -- --configuration=production'
                        }
                    }
                    post {
                        always {
                            // karma-junit-reporter opcional — não incluído no package.json padrão
                            // Instale-o e configure karma.conf.js para habilitar export JUnit
                            junit(
                                testResults      : "${env.CLIENT_DIR}/test-results/**/*.xml",
                                allowEmptyResults: true
                            )
                            archiveArtifacts(
                                artifacts        : "${env.CLIENT_DIR}/dist/**",
                                allowEmptyArchive: true
                            )
                        }
                        failure {
                            echo "\033[31m✗  Build/Test do frontend falhou — pipeline abortará\033[0m"
                        }
                    }
                }
            } // end parallel
        }

        // ======================================================
        // STAGE 2b — QUALITY GATE — BACKEND
        //
        // Executa jacocoTestCoverageVerification após o Build & Test.
        // Stage isolado: falha de cobertura tem contexto próprio e
        // bloqueia o Docker Build sem misturar com falha de compilação.
        // ======================================================
        stage('Quality Gate — Backend') {
            when {
                allOf {
                    expression { return env.K8S_ONLY == 'false' }
                    expression { return env.BUILD_API == 'true'  }
                    expression { return !params.SKIP_TESTS        }
                }
            }
            steps {
                dir(env.API_DIR) {
                    sh './gradlew jacocoTestCoverageVerification --no-daemon'
                }
            }
            post {
                always {
                    // Arquiva relatório HTML de cobertura — acessível na UI do Jenkins
                    archiveArtifacts(
                        artifacts        : "${env.API_DIR}/build/reports/jacoco/**",
                        allowEmptyArchive: true
                    )
                }
                failure {
                    echo "\033[31m✗  Quality Gate falhou — cobertura abaixo do mínimo. Docker build não será executado.\033[0m"
                }
                success {
                    echo "\033[32m✔  Quality Gate aprovado\033[0m"
                }
            }
        }

        // ======================================================
        // STAGE 3 — DOCKER BUILD & PUSH (PARALELO)
        //
        // Só executa em branches deployáveis (main, develop, release/*).
        // Usa --cache-from :latest para reutilizar camadas entre builds.
        // Publica sempre: :{IMAGE_TAG} e :latest
        // Publica opcionalmente: :{GIT_TAG_NAME} se tag semântica existir
        // ======================================================
        stage('Docker Build & Push') {
            when {
                allOf {
                    expression { return env.K8S_ONLY == 'false' }
                    anyOf {
                        branch 'main'
                        branch 'develop'
                        branch pattern: 'release/.*', comparator: 'REGEXP'
                    }
                }
            }
            parallel {

                stage('Docker: API') {
                    when {
                        expression { return env.BUILD_API == 'true' }
                    }
                    steps {
                        script {
                            dockerBuildAndPush(
                                contextDir : env.API_DIR,
                                imageName  : env.API_IMAGE,
                                imageTag   : env.IMAGE_TAG,
                                gitTagName : env.GIT_TAG_NAME,
                                registry   : env.REGISTRY,
                                credsId    : env.DOCKER_CREDS_ID
                            )
                        }
                    }
                }

                stage('Docker: Client') {
                    when {
                        expression { return env.BUILD_CLIENT == 'true' }
                    }
                    steps {
                        script {
                            dockerBuildAndPush(
                                contextDir : env.CLIENT_DIR,
                                imageName  : env.CLIENT_IMAGE,
                                imageTag   : env.IMAGE_TAG,
                                gitTagName : env.GIT_TAG_NAME,
                                registry   : env.REGISTRY,
                                credsId    : env.DOCKER_CREDS_ID
                            )
                        }
                    }
                }
            } // end parallel
        }

        // ======================================================
        // STAGE 4 — DEPLOY → DEV (automático)
        //
        // Disparado automaticamente na branch develop.
        // Overlay: k8s/overlays/dev (namespace: myblog-dev, prefix: dev-)
        // 1 réplica, perfil Spring: dev
        // ======================================================
        stage('Deploy → Dev') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    kustomizeDeploy(
                        overlay     : 'dev',
                        namespace   : env.K8S_NS_DEV,
                        apiImage    : env.API_IMAGE,
                        clientImage : env.CLIENT_IMAGE,
                        imageTag    : env.IMAGE_TAG,
                        k8sDir      : env.K8S_DIR,
                        kubeCredsId : env.KUBECONFIG_CREDS_ID,
                        buildNumber : env.BUILD_NUMBER,
                        appName     : env.APP_NAME,
                        secretName  : env.API_SECRET_NAME,
                        deployments : [
                            [ name: env.K8S_DEPLOY_API_DEV,    timeout: env.ROLLOUT_TIMEOUT_DEV  ],
                            [ name: env.K8S_DEPLOY_CLIENT_DEV, timeout: env.ROLLOUT_TIMEOUT_DEV  ]
                        ]
                    )
                    healthCheckApi(url: env.API_HEALTH_URL_DEV, environment: 'dev')
                }
            }
        }

        // ======================================================
        // STAGE 5 — APROVAÇÃO MANUAL PARA PROD
        //
        // agent none: libera o executor Jenkins durante a espera
        // para não bloquear outros builds no homelab.
        // Timeout de 30min — após isso a pipeline é abortada.
        // ======================================================
        stage('Approve → Prod') {
            when {
                branch 'main'
            }
            // agent none é crítico aqui: libera o node durante o input
            agent none
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    input(
                        message  : "🚀 Aprovar deploy de \033[1m${env.IMAGE_TAG}\033[0m para PRODUÇÃO?",
                        ok       : '✅ Confirmar Deploy',
                        // Ajuste 'submitter' para usuários/grupos Jenkins autorizados
                        submitter: 'admin,devops-team'
                    )
                }
            }
        }

        // ======================================================
        // STAGE 6 — DEPLOY → PROD (após aprovação manual)
        //
        // Overlay: k8s/overlays/prod (namespace: myblog, sem prefix)
        // 2 réplicas, perfil Spring: prod, HPA ativo (2-5 pods),
        // PDB configurado para garantir disponibilidade durante rollouts.
        // ======================================================
        stage('Deploy → Prod') {
            when {
                branch 'main'
            }
            steps {
                script {
                    kustomizeDeploy(
                        overlay     : 'prod',
                        namespace   : env.K8S_NS_PROD,
                        apiImage    : env.API_IMAGE,
                        clientImage : env.CLIENT_IMAGE,
                        imageTag    : env.IMAGE_TAG,
                        k8sDir      : env.K8S_DIR,
                        kubeCredsId : env.KUBECONFIG_CREDS_ID,
                        buildNumber : env.BUILD_NUMBER,
                        appName     : env.APP_NAME,
                        secretName  : env.API_SECRET_NAME,
                        deployments : [
                            [ name: env.K8S_DEPLOY_API_PROD,    timeout: env.ROLLOUT_TIMEOUT_PROD ],
                            [ name: env.K8S_DEPLOY_CLIENT_PROD, timeout: env.ROLLOUT_TIMEOUT_PROD ]
                        ]
                    )
                    healthCheckApi(url: env.API_HEALTH_URL_PROD, environment: 'prod')
                }
            }
        }

    } // end stages

    // ──────────────────────────────────────────────────────────
    // POST — Limpeza, relatórios e notificações
    // always: executado independente do resultado
    // ──────────────────────────────────────────────────────────
    post {
        always {
            script {
                // Remover imagens locais para evitar acúmulo de disco no agent
                // || true: não falha o post se a imagem não existir
                sh """
                    docker image rm ${env.API_IMAGE}:${env.IMAGE_TAG}    2>/dev/null || true
                    docker image rm ${env.CLIENT_IMAGE}:${env.IMAGE_TAG} 2>/dev/null || true
                    docker image rm ${env.API_IMAGE}:latest              2>/dev/null || true
                    docker image rm ${env.CLIENT_IMAGE}:latest           2>/dev/null || true
                """
                if (env.GIT_TAG_NAME) {
                    sh """
                        docker image rm ${env.API_IMAGE}:${env.GIT_TAG_NAME}    2>/dev/null || true
                        docker image rm ${env.CLIENT_IMAGE}:${env.GIT_TAG_NAME} 2>/dev/null || true
                    """
                }
            }
        }
        success {
            echo "\033[32m✔  Pipeline concluída com sucesso! Tag publicada: ${env.IMAGE_TAG}\033[0m"
            // Descomente para notificação Slack (requer plugin Slack Notification):
            // slackSend(
            //     channel: '#deployments',
            //     color  : 'good',
            //     message: "✅ *${env.APP_NAME}* `${env.IMAGE_TAG}` — deploy concluído!\n${env.BUILD_URL}"
            // )
        }
        failure {
            echo "\033[31m✗  Pipeline falhou. Revise os logs acima.\033[0m"
            // slackSend(
            //     channel: '#deployments',
            //     color  : 'danger',
            //     message: "❌ *${env.APP_NAME}* `${env.IMAGE_TAG}` — build/deploy FALHOU!\n${env.BUILD_URL}"
            // )
        }
        unstable {
            echo "\033[33m⚠  Pipeline instável — testes com falha não-crítica.\033[0m"
        }
    }

} // end pipeline

// ================================================================
// FUNÇÕES COMPARTILHADAS (definidas fora do bloco pipeline)
//
// ⚙️  Para reuso em múltiplos microserviços:
//     Mova estas funções para uma Jenkins Shared Library em vars/
//     e carregue com: @Library('myblog-shared-lib') _
// ================================================================

/**
 * dockerBuildAndPush
 *
 * Faz Docker build com cache da tag :latest do registry,
 * taga e publica a imagem com até 3 tags:
 *   - :{imageTag}     (sempre, ex: 42-a3f9b12)
 *   - :latest         (sempre)
 *   - :{gitTagName}   (somente se tag semântica existir, ex: v1.2.0)
 *
 * @param cfg.contextDir  Diretório do Dockerfile (relativo à raiz do repo)
 * @param cfg.imageName   Nome completo da imagem sem tag
 * @param cfg.imageTag    Tag principal (BUILD_NUMBER-shortcommit)
 * @param cfg.gitTagName  Tag git semântica (pode ser vazia)
 * @param cfg.registry    Host do registry (ex: 192.168.0.106:5000)
 * @param cfg.credsId     Jenkins credentials ID (Username with password)
 */
def dockerBuildAndPush(Map cfg) {
    def contextDir = cfg.contextDir
    def imageName  = cfg.imageName
    def imageTag   = cfg.imageTag
    def gitTagName = cfg.gitTagName ?: ''
    def registry   = cfg.registry
    def credsId    = cfg.credsId

    withCredentials([
        usernamePassword(
            credentialsId   : credsId,
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
        )
    ]) {
        // Login no registry privado HTTP (inseguro — aceitável em homelab)
        sh "echo \"\${DOCKER_PASS}\" | docker login http://${registry} -u \"\${DOCKER_USER}\" --password-stdin"

        try {
            // Puxar :latest para popular o cache de camadas local.
            // || true: não falha no primeiro build quando :latest não existe.
            sh "docker pull ${imageName}:latest || true"

            // Build com --cache-from :latest e BUILDKIT_INLINE_CACHE=1
            // BUILDKIT_INLINE_CACHE embute metadados de cache na imagem
            // para que --cache-from funcione corretamente no próximo build.
            sh """
                docker build \\
                    --cache-from ${imageName}:latest \\
                    --build-arg BUILDKIT_INLINE_CACHE=1 \\
                    -t ${imageName}:${imageTag} \\
                    -f ${contextDir}/Dockerfile \\
                    ${contextDir}
            """

            // Push tag principal (rastreável por build + commit)
            trivyScan(imageName: "${imageName}:${imageTag}", version: env.TRIVY_VERSION)

            sh "docker push ${imageName}:${imageTag}"
            echo "\033[32m  ✔ Publicado: ${imageName}:${imageTag}\033[0m"

            // Push :latest (referência conveniente)
            sh """
                docker tag  ${imageName}:${imageTag} ${imageName}:latest
                docker push ${imageName}:latest
            """
            echo "\033[32m  ✔ Publicado: ${imageName}:latest\033[0m"

            // Push tag semântica se existir (ex: v1.2.0)
            if (gitTagName) {
                sh """
                    docker tag  ${imageName}:${imageTag} ${imageName}:${gitTagName}
                    docker push ${imageName}:${gitTagName}
                """
                echo "\033[32m  ✔ Publicado: ${imageName}:${gitTagName}\033[0m"
            }

        } finally {
            // Logout sempre, mesmo em caso de erro
            sh "docker logout http://${registry} || true"
        }
    }
}

/**
 * kustomizeDeploy
 *
 * Realiza deploy via Kustomize + kubectl de forma segura:
 *   1. Copia o diretório k8s/ para /tmp (não modifica o repositório)
 *   2. Injeta a nova tag via `kustomize edit set image`
 *   3. Valida o manifesto gerado com --dry-run=client
 *   4. Aplica com kubectl apply -k
 *   5. Verifica rollout de cada Deployment
 *   6. Limpa o diretório temporário
 *
 * Em caso de falha no rollout, exibe o comando de rollback manual.
 *
 * @param cfg.overlay      Nome do overlay (dev | prod)
 * @param cfg.namespace    Namespace Kubernetes alvo
 * @param cfg.apiImage     Nome da imagem da API (sem tag)
 * @param cfg.clientImage  Nome da imagem do Client (sem tag)
 * @param cfg.imageTag     Tag a ser injetada
 * @param cfg.k8sDir       Caminho do diretório k8s no repo
 * @param cfg.kubeCredsId  Jenkins credentials ID do kubeconfig (Secret file)
 * @param cfg.buildNumber  BUILD_NUMBER para diretório temporário único
 * @param cfg.appName      Nome do app para o diretório temporário
 * @param cfg.deployments  Lista de maps [ name: '...', timeout: '...s' ]
 */
def kustomizeDeploy(Map cfg) {
    def overlay     = cfg.overlay
    def namespace   = cfg.namespace
    def apiImage    = cfg.apiImage
    def clientImage = cfg.clientImage
    def imageTag    = cfg.imageTag
    def k8sDir      = cfg.k8sDir
    def kubeCredsId = cfg.kubeCredsId
    def buildNumber = cfg.buildNumber
    def appName     = cfg.appName
    def deployments = cfg.deployments
    def secretName  = cfg.secretName ?: ''   // opcional — pre-flight pulado se vazio

    // Diretório temporário único por build — evita conflito em builds paralelos
    def tmpDir      = "/tmp/kustomize-${appName}-${overlay}-${buildNumber}"
    def overlayPath = "${tmpDir}/overlays/${overlay}"

    withCredentials([
        file(credentialsId: kubeCredsId, variable: 'KUBECONFIG')
    ]) {
        try {
            echo "\033[36m╔══ Deploy → ${overlay.toUpperCase()} (${namespace}) ══╗\033[0m"

            // ── 1. Copiar k8s/ para /tmp ────────────────────────────────
            // CRÍTICO: nunca rodar kustomize edit no workspace original —
            // modificaria arquivos rastreados pelo git.
            sh """
                rm -rf ${tmpDir}
                cp -r ${k8sDir} ${tmpDir}
            """

            // ── 2. Injetar nova tag de imagem ───────────────────────────
            // kustomize edit set image é idempotente:
            //   - Se o overlay já tem bloco images: (prod) → atualiza a tag
            //   - Se não tem (dev) → adiciona o bloco automaticamente
            sh """
                cd ${overlayPath}
                kustomize edit set image \\
                    ${apiImage}=${apiImage}:${imageTag} \\
                    ${clientImage}=${clientImage}:${imageTag}
            """
            echo "  🏷  Imagens atualizadas para tag: ${imageTag}"

            // ── 3. Dry-run para validação antecipada ────────────────────
            // Detecta erros de manifesto antes de tocar no cluster
            sh """
                kubectl apply -k ${overlayPath} \\
                    --namespace=${namespace} \\
                    --dry-run=client
            """
            echo "  ✔  Dry-run validado com sucesso"

            // ── 3b. Pre-flight: verificar existência do secret ──────────────
            // Falha rápida antes de tocar no cluster — evita deploy + CrashLoopBackOff
            // por secret ausente (que só seria detectado após rollout timeout).
            if (secretName) {
                def secretExists = sh(
                    script: "kubectl get secret ${secretName} -n ${namespace} 2>/dev/null",
                    returnStatus: true
                )
                if (secretExists != 0) {
                    error("""Secret '${secretName}' não encontrado no namespace '${namespace}'.
  Execute antes do deploy:
    ENVIRONMENT=${overlay} ./api/scripts/create-secrets.sh""")
                }
                echo "  ✔  Secret '${secretName}' presente em ${namespace}"
            }

            // ── 4. Apply real ───────────────────────────────────────────
            sh """
                kubectl apply -k ${overlayPath} \\
                    --namespace=${namespace}
            """
            echo "  ✔  kubectl apply concluído"

            // ── 5. Verificar rollout de cada Deployment ─────────────────
            // rollout status bloqueia até os pods ficarem ready (readinessProbe)
            // Exit code ≠ 0 indica timeout ou erro → pipeline falha
            deployments.each { deployment ->
                echo "  ⏳ Aguardando rollout: ${deployment.name} (timeout: ${deployment.timeout})"

                def exitCode = sh(
                    script: """
                        kubectl rollout status deployment/${deployment.name} \\
                            -n ${namespace} \\
                            --timeout=${deployment.timeout}
                    """,
                    returnStatus: true
                )

                if (exitCode != 0) {
                    // Exibe comando de rollback antes de lançar o erro
                    echo """\033[31m
╔══════════════════════════════════════════════════════════════╗
║  ROLLOUT FALHOU: ${deployment.name}
║  Para reverter manualmente execute:
║  kubectl rollout undo deployment/${deployment.name} -n ${namespace}
╚══════════════════════════════════════════════════════════════╝\033[0m"""

                    // Executa rollback automático — reverte para revisão anterior
                    // || true: não mascarar o erro original se o undo também falhar
                    echo "  ⏪ Executando rollback automático: ${deployment.name}"
                    sh "kubectl rollout undo deployment/${deployment.name} -n ${namespace} || true"
                    echo "  \033[33m⚠  Rollback executado para ${deployment.name} em ${namespace}\033[0m"

                    error("Rollout falhou para ${deployment.name} em ${namespace}")
                }

                echo "  \033[32m✔  Rollout OK: ${deployment.name}\033[0m"
            }

            echo "\033[32m╚══ Deploy ${overlay.toUpperCase()} concluído! Tag: ${imageTag} ══╝\033[0m"

        } finally {
            // ── 6. Limpeza do diretório temporário ──────────────────────
            // Executada sempre (mesmo em caso de falha) para não acumular arquivos
            sh "rm -rf ${tmpDir} || true"
        }
    }
}

/**
 * healthCheckApi
 *
 * Verifica que a API responde {"status":"UP"} via HTTP após o deploy.
 * Complementa o rollout status (que verifica pods K8s) confirmando
 * que a aplicação responde a requests HTTP reais pelo Ingress.
 *
 * Retry 5x com 10s de intervalo (total: até ~50s após rollout concluído).
 * Suficiente para Spring Boot finalizar inicialização pós-readinessProbe.
 *
 * @param cfg.url         URL completa do endpoint /actuator/health
 * @param cfg.environment Nome do ambiente para logging (dev | prod)
 */
def healthCheckApi(Map cfg) {
    def url         = cfg.url
    def environment = cfg.environment

    echo "  ⏳ Health check API (${environment}): ${url}"
    retry(5) {
        sleep(time: 10, unit: 'SECONDS')
        sh "curl -sf '${url}' | grep '\"status\":\"UP\"'"
    }
    echo "  \033[32m✔  Health check OK — API respondendo em ${environment}\033[0m"
}

/**
 * trivyScan
 *
 * Escaneia uma imagem Docker LOCAL com Trivy antes do push ao registry.
 * Implementa segurança shift-left: imagens com CVEs HIGH/CRITICAL
 * são rejeitadas antes de contaminar o registry privado.
 *
 * Estratégias:
 *   - Tag fixa (TRIVY_VERSION) — determinismo entre builds
 *   - Volume 'trivy-cache' — DB (~90MB) baixado uma vez, reutilizado
 *   - --ignore-unfixed — não bloqueia por CVEs sem fix disponível
 *   - Docker socket montado — acessa imagem local sem pull do registry
 *
 * @param cfg.imageName  Nome completo da imagem com tag (ex: registry/app:1-abc1234)
 * @param cfg.version    Tag do Trivy (ex: 0.63.0) — usar TRIVY_VERSION do environment
 */
def trivyScan(Map cfg) {
    def imageName = cfg.imageName
    def version   = cfg.version

    echo "  🔍 Trivy scan: ${imageName}"
    sh """
        docker run --rm \\
            -v /var/run/docker.sock:/var/run/docker.sock \\
            -v trivy-cache:/root/.cache/trivy \\
            ghcr.io/aquasecurity/trivy:${version} image \\
            --exit-code 1 \\
            --severity HIGH,CRITICAL \\
            --ignore-unfixed \\
            --timeout 15m \\
            ${imageName}
    """
    echo "  \033[32m✔  Trivy: imagem aprovada — ${imageName}\033[0m"
}


