# MyBlog

Plataforma fullstack de publicação de artigos. Usuários autenticados criam, editam e excluem postagens. Visitantes navegam, buscam por título ou tag e postam comentários.

Originalmente um exercício de avaliação técnica, o projeto evoluiu para um sistema em produção implantado em homelab (k3s, Docker, Jenkins CI/CD).

## STACK

| Camada      | Tecnologia                                |
|-------------|-------------------------------------------|
| Backend     | Java 21, Spring Boot 3.4.3                |
| Segurança   | Spring Security 6, JWT (jjwt 0.12.3)      |
| Persistência| Spring Data JPA, Hibernate, Flyway, H2    |
| Build       | Gradle (Groovy DSL)                       |
| Frontend    | Angular 10.1.6, Angular Material 10.2.7   |
| Servidor    | Nginx (SPA)                               |
| Runtime     | Docker, docker-compose                    |
| Deploy alvo | k3s (Kubernetes), Registry privado        |
| CI/CD       | Jenkins                                   |

## ESTRUTURA DO PROJETO

```
myblog/
├── api/          ← Spring Boot (Java 21)
├── client/       ← Angular 10 + Nginx
├── k8s/          ← Manifests Kubernetes (kustomize)
├── docker-compose.yml
└── docker-compose-windows.yml
```

## COMO RODAR COM DOCKER

### Linux

```bash
# Build
docker-compose build

# Subir
docker-compose up
```

### Windows (PowerShell)

```powershell
# Build
docker-compose -f docker-compose-windows.yml build

# Subir
docker-compose -f docker-compose-windows.yml up
```

Após iniciar, acesse: **http://localhost:8082/**

Para encerrar: `docker-compose down` (Linux) ou `docker-compose -f docker-compose-windows.yml down` (Windows).

## BUILD SOMENTE DA API

No diretório `api/`, execute:

### Linux

```bash
# Defina uma chave secreta forte — nunca use a mesma em produção
JWT_SECRET=<sua-chave-secreta> ./gradlew build
```

### Windows (PowerShell)

**IMPORTANTE**: Execute os comandos individualmente. Certifique-se de que o Java 21 está instalado.

| Comando                     | Ação                                  |
|-----------------------------|---------------------------------------|
| `.\gradlew clean`           | Limpa builds anteriores               |
| `.\gradlew --stop`          | Encerra o Daemon                      |
| `$env:JAVA_HOME="..."`      | Configura JDK 21 temporariamente      |
| `$env:JWT_SECRET="..."`     | Define chave JWT para autenticação    |
| `.\gradlew build --no-daemon` | Build sem Daemon (processo limpo)   |

```powershell
$env:JAVA_HOME="C:\\Program Files\\Java\\jdk-21"
$env:JWT_SECRET="<sua-chave-secreta>"
.\gradlew build
```

O arquivo `api-myblog.jar` será gerado em `api/build/libs/`.

### Rodando o JAR

```bash
# Linux
JWT_SECRET=<sua-chave-secreta> java -jar api-myblog.jar
```

```powershell
# Windows
$env:JWT_SECRET="<sua-chave-secreta>"
java -jar api-myblog.jar
```

> ⚠️ **Segurança**: sempre use uma chave forte e única por ambiente. Nunca reutilize a mesma chave em desenvolvimento e produção.

## USUÁRIOS DEFAULT

Inseridos via Flyway migration ao iniciar a aplicação:

| Perfil        | Usuário          | Senha    |
|---------------|------------------|----------|
| Administrador | adm@email.com    | pwd123   |
| Usuário       | (ver migration)  | senha123 |

## PRINCIPAIS ENDPOINTS

| Método | Endpoint                      | Auth | Descrição                            |
|--------|-------------------------------|------|--------------------------------------|
| GET    | /news                         | Não  | Listagem paginada (busca por título) |
| GET    | /news/{id}                    | Não  | Postagem completa com comentários    |
| GET    | /news/topic?tag={tag}         | Não  | Filtra por tag                       |
| POST   | /news                         | Sim* | Cria postagem                        |
| PUT    | /news/{id}                    | Sim* | Edita postagem                       |
| DELETE | /news/{id}                    | Sim* | Exclui postagem                      |
| POST   | /news/{id}                    | Não  | Adiciona comentário                  |
| POST   | /auth/login                   | Não  | Autenticação JWT                     |
| POST   | /auth/refresh                 | Não  | Renova access token                  |
| POST   | /auth/logout                  | Sim  | Invalida refresh token               |

> *⚠️ Endpoints de escrita ainda não protegidos corretamente — bug conhecido no `SecurityConfig`.

### Paginação

```
GET http://localhost:8080/news?page=0&size=10
GET http://localhost:8080/news?title=titulo&page=0&size=10
```

## H2 DATABASE (desenvolvimento)

```
http://localhost:8080/h2-console
```

> ⚠️ Disponível apenas em desenvolvimento. Dados são perdidos ao reiniciar o container.

## DEPLOY KUBERNETES (k3s)

Manifests em `k8s/`. Usa Kustomize com overlays para `dev` e `prod`.

```bash
# Deploy no namespace myblog
kubectl apply -k k8s/overlays/dev
```

## TODO / ROADMAP

- [x] Build Windows
- [ ] Corrigir SecurityConfig — proteger `POST`/`PUT`/`DELETE /news`
- [ ] Criar `application-prod.yml` (PostgreSQL, H2 desabilitado)
- [ ] Cadastro de usuário (`POST /users`)
- [ ] Vincular autor da postagem ao usuário autenticado
- [ ] Permitir apenas ao autor excluir suas postagens
- [ ] Editar e excluir comentários
- [ ] Migrar de H2 para PostgreSQL
- [ ] Atualizar Angular (10 → versão atual) e dependências