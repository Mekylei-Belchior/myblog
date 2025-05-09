FROM node:14 AS builder
WORKDIR /app

# Copia apenas os arquivos necessários para instalar as dependências
COPY package.json package-lock.json ./

# Instala as dependências e utiliza cache
RUN --mount=type=cache,target=/root/.npm \
    npm install

# Copia o restante do código-fonte
COPY . .

# Executa o build do projeto
RUN npm run build --configuration=production

# Estágio final
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Copia os arquivos buildados do estágio anterior
COPY --from=builder /app/dist/myblog /usr/share/nginx/html

# Copia a configuração personalizada do Nginx
COPY config/nginx-custom.conf /etc/nginx/conf.d/default.conf

# Expõe a porta 80
EXPOSE 80

# Inicia o Nginx
ENTRYPOINT ["nginx","-g","daemon off;"]
