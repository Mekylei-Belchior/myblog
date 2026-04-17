# MyBlog — Client

Frontend Angular da plataforma MyBlog. SPA servida via Nginx.

- **Angular** 10.1.6 + **Angular Material** 10.2.7
- Comunicação com a API via `NewsService` e `AuthService` (nunca `HttpClient` diretamente nos componentes)
- Autenticação JWT com refresh automático (`JwtInterceptor`)
- Módulos lazy-loaded: `HomeModule`, `NewsModule`

## Desenvolvimento local

```bash
# Instalar dependências
npm install

# Subir servidor de desenvolvimento (porta 4200)
ng serve

# A API deve estar rodando em http://localhost:8080
# Configurado em src/environments/environment.ts
```

## Build de produção

```bash
ng build --prod
```

Os artefatos são gerados em `dist/`. O `Dockerfile` usa build multi-stage (Node → Nginx).

## Docker (via docker-compose na raiz)

Ver instruções no [README principal](../README.md).

## Variável de ambiente

| Variável      | Uso                        | Valor local              | Valor produção |
|---------------|----------------------------|--------------------------|----------------|
| `API_BASE_URL` | URL base da API (build arg) | `http://127.0.0.1:8080` | `""` (vazio)   |

Em produção, a URL vazia faz com que as chamadas sejam relativas e roteadas pelo Nginx.
