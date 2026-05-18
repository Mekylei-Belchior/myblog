FROM node:20-alpine AS builder
WORKDIR /app

# Copia apenas os arquivos necessários para instalar as dependências
COPY package.json package-lock.json ./

# Instala as dependências e utiliza cache
RUN --mount=type=cache,target=/root/.npm \
    npm ci --legacy-peer-deps

# Copia o restante do código-fonte
COPY . .

# Executa o build do projeto (NODE_OPTIONS necessário para webpack4/OpenSSL3 no Node 17+)
RUN NODE_OPTIONS=--openssl-legacy-provider npm run build -- --configuration=production

# Estágio final
FROM nginx:alpine
# Atualiza pacotes do sistema para corrigir CVEs (ex: nghttp2-libs CVE-2026-27135)
RUN apk upgrade --no-cache
WORKDIR /usr/share/nginx/html

# Copia os arquivos buildados do estágio anterior
COPY --from=builder /app/dist/myblog /usr/share/nginx/html

# Copia a configuração personalizada do Nginx
COPY config/nginx-custom.conf /etc/nginx/conf.d/default.conf

# Expõe a porta 80
EXPOSE 80

# Inicia o Nginx
ENTRYPOINT ["nginx","-g","daemon off;"]
