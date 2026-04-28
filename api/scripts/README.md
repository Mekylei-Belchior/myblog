# Scripts — MyBlog

## create-secrets.sh

Cria (ou atualiza) o Kubernetes Secret `myblog-secrets` nos namespaces do cluster.

### Quando executar

| Situação | Ação |
|---|---|
| Antes do **primeiro deploy** | Executar para `dev` e `prod` |
| **Rotação de secrets** (JWT ou DB) | Executar no ambiente afetado + rollout restart |
| Após recriar namespace ou cluster | Executar novamente |

### Como executar

```bash
# Dev (namespace: myblog-dev)
ENVIRONMENT=dev ./api/scripts/create-secrets.sh

# Prod (namespace: myblog)
ENVIRONMENT=prod ./api/scripts/create-secrets.sh
```

### O que é criado

Secret `myblog-secrets` com as chaves:

| Chave | Descrição | Obrigatório |
|---|---|---|
| `jwt-secret` | Chave HS256 para assinar JWTs (base64) | ✅ Sim |
| `db-username` | Usuário do PostgreSQL | ✅ Sim |
| `db-password` | Senha do PostgreSQL | ⚠️ Necessário em prod |

### Gerar JWT_SECRET seguro

```bash
openssl rand -base64 64 | tr -d '\n'
```

O valor gerado deve ser fornecido quando o script solicitar `JWT_SECRET`.

### ⚠️ Regras importantes

- **Nunca versionar** valores reais neste repositório
- **Nunca automatizar** este script em pipelines — ele é propositalmente interativo
- O script é **idempotente**: pode ser re-executado para rotação de secrets sem risco
- Se o namespace ainda não existir, crie-o primeiro:
  ```bash
  kubectl create namespace myblog-dev
  kubectl create namespace myblog
  ```

### Verificar após a criação

```bash
# Confirmar que todas as chaves estão presentes
kubectl describe secret myblog-secrets -n myblog-dev

# Verificar no namespace prod
kubectl describe secret myblog-secrets -n myblog
```

