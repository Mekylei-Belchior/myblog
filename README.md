# Technical evaluation for Full Stack Developer

MyBlog é uma simples aplicação fullstack desenvolvida utilizando Spring Boot e H2 Database no backend e Angular no frontend.

## INFORMAÇÕES

- Java 21
- Spring Boot 3.4.3
- Spring security e JWT
- Flyway
- H2 Database
- Angular 10.1.6
- Angular Material 10.2.7

## COMO RODAR O PROJETO UTILIZANDO O DOCKER

Você pode rodar o projeto seguindo os passos abaixo:

1. Com o docker rodando em sua máquina, abra um terminal no diretório raíz do projeto ao qual contém os arquivos do docker-compose e rode os comandos para construir e rodar a aplicação.

Linux
```bash

docker-compose build
docker-compose build --no-cache (build desconsiderando caches)
```

Windows (PowerShell)
```powershell

docker-compose -f docker-compose-windows.yml build
docker-compose -f docker-compose-windows.yml build --no-cache
```


1. Rode os comandos abaixo e aguarde o processamento das informações. Após a conclusão acesse a aplicação no endereço: http://localhost:8082/

Linux
```bash

docker-compose up

```
Windows (PowerShell)
```powershell

docker-compose -f docker-compose-windows.yml up

```

1. Para finalizar o container, rode o comando `docker-compose down` para o Linux e `docker-compose -f docker-compose-windows.yml down` para o Windows.

## BUILD SOMENTE DA API

Caso queria testar somente a API, siga os passos abaixo:

1. No diretório raíz da API `api/` abra o terminal apontando para o diretório citado e rode o comando:

Linux
```bash

JWT_SECRET=gqUrQCpx4KT4Q9Zig5lcDyVVTH023MZ/cJcFseu77PU= ./gradlew build

```

Windows (PowerShell)

**IMPORTANTE**: Execute os camandos individualmente para evitar erros. Tenha certeza que o Java 21 esteja instalado.
Se necessário, utilizei todos os comandos listados na tabela abaixo. Geralmente, somente os comandos
listados no bloco de código já devem construir a aplicação corretamente.

O caminho do Java 21 pode ser diferente dependendo de como a instalação foi realizada. Se necessário faça os ajustes
necessários para que o caminho esteja apontando para a versão correta do Java 21.

| Comando                     | Ação                                  |
|-----------------------------|---------------------------------------|
| .\gradlew clean             | Limpa builds anteriores               |
| .\gradlew --stop            | encerra Daemon                        |
| $env:JAVA_HOME="..."        | Configura JDK 21 temporariamente      |
| $env:JWT_SECRET="..."       | Define chave JWT para autenticação    |
| .\gradlew build --no-daemon | Build sem Daemon (processo limpo)     |

```powershell
$env:JAVA_HOME="C:\\Program Files\\Java\\jdk-21"
$env:JWT_SECRET="gqUrQCpx4KT4Q9Zig5lcDyVVTH023MZ/cJcFseu77PU="
.\gradlew build
```
Após o build finalizar, o arquivo resultante `api-myblog.jar` estará disponível no diretório `api/build/libs/`.

1. Para rodar a API, abra o terminal apontando para o diretório supracitado que contém o arquivo `.jar` e execute o comando:

Linux
```bash

JWT_SECRET=gqUrQCpx4KT4Q9Zig5lcDyVVTH023MZ/cJcFseu77PU= java -jar api-myblog.jar

```

Windows (PowerShell)
```powershell

java -jar myblog-api.jar
```

### PRINCIPAIS ENDPOINTS

**Todas as postagens**
```
GET http://localhost:8080/news
```

**Postagem específica**
```
GET http://localhost:8080/news/id-da-postagem
```

**Busca por título da postagem**
```
GET http://localhost:8080/news?title=titulo-da-postagem
```

**Busca por tag**
```
GET http://localhost:8080/news/topic?tag=nome-da-tag
```

**cria uma nova postagem**
```
POST http://localhost:8080/news
```

**Edita uma postagem**
```
PUT http://localhost:8080/news/id-da-postagem
```

**Deleta uma postagem**
```
DELETE http://localhost:8080/news/id-da-postagem
```

### PAGINAÇÃO E ORDENAÇÃO

Obtém as postagens definindo a quantidade de itens por página e/ou especificando uma página. Por padrão, as postagens são ordenadas de forma decrescente.

**Postagem paginada**
```
GET http://localhost:8080/news?page=paginasize=quantidade-de-itens-por-pagina
```


**Busca por título paginada**
```
GET http://localhost:8080/news?title=titulo&page=pagina&size=quantidade-de-itens-por-pagina
```

## ACESSAR O H2 DATABASE

```
http://localhost:8080/h2-console
```

### TODO

- [X] Ajustar build windows
- [ ] Criar cadastro de usuário
- [ ] Somente usuário logado pode criar postagem
- [ ] Editar e excluir comentário
- [ ] Usuário que criou a postagem pode excluí-la
- [ ] Atualizar o Angular, Angular Material e outras dependências