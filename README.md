# Technical evaluation for Full Stack Developer

MyBlog é uma simples aplicação fullstack desenvolvida utilizando Spring Boot e H2 Database no back end e Angular no front end.

## INFORMAÇÕES  

- Java 11
- Spring Boot 2.5.4
- H2 Database
- Angular 10.1.6
- Angular Material 10.2.7  

## COMO RODAR O PROJETO UTILIZANDO O DOCKER

Você pode rodar o projeto seguindo os passos abaixo:  

1. Com o docker rodando em sua máquina, abra o prompt de comando (no caso do Windows), e vá para o diretório raíz do projeto ao qual contém o arquivo `docker-compose.yml`.

1. Rode o comando `docker-compose up`. Aguarde o processamento das informações e após a conclusão acesse a aplicação no endereço: http://localhost:8082/

1. Para finalizar o container, rode o comando `docker-compose down`.

## BUILD SOMENTE DA API

Caso queria testar somente a API, siga os passos abaixo:

1. No diretório raíz da API `api/` abra o prompt apontando para o diretório citado e rode o comando `mvn clean package`. Após o build finalizar, o arquivo resultante `api-myblog.jar` estará disponível no diretório `api/target/`.

1. Para rodar a API, abra o prompt apontando para o diretório supracitado que contém o arquivo `.jar` e execute o comando `java -jar api-myblog.jar`.

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

Obtém as postagens definindo a quantidade de itens por página e/ou especificando uma página. Por padrão, as postagem são ordenadas de forma decrescente.

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