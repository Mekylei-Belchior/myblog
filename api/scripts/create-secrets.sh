#!/bin/bash
# =============================================================================
# create-secrets.sh — MyBlog Kubernetes Secrets
#
# Cria (ou atualiza) o Secret 'myblog-secrets' no namespace correto,
# de forma INTERATIVA e IDEMPOTENTE, sem deixar rastros em logs ou
# histórico de shell.
#
# ⚠️  NUNCA automatize este script na pipeline.
#     Ele deve ser executado manualmente por um operador com kubeconfig
#     válido appontando para o cluster alvo, ANTES do primeiro deploy
#     ou durante rotação de secrets.
#
# Uso:
#   ENVIRONMENT=dev  ./api/scripts/create-secrets.sh
#   ENVIRONMENT=prod ./api/scripts/create-secrets.sh
#
# Secrets criados (referenciados em k8s/base/deployment.yaml):
#   jwt-secret   → JWT_SECRET da aplicação (base64-encoded, HS256)
#   db-username  → usuário do banco de dados PostgreSQL
#   db-password  → senha do banco de dados PostgreSQL
#
# Para gerar um JWT_SECRET seguro:
#   openssl rand -base64 64 | tr -d '\n'
# =============================================================================

set -euo pipefail

ENVIRONMENT="${ENVIRONMENT:-dev}"

# ---------------------------------------------------------------------------
# Mapeamento de namespaces por ambiente
# ---------------------------------------------------------------------------
case "$ENVIRONMENT" in
  dev)
    NAMESPACE="myblog-dev"
    ;;
  prod)
    NAMESPACE="myblog"
    ;;
  *)
    echo "❌ Ambiente inválido: '$ENVIRONMENT'. Use: dev | prod"
    exit 1
    ;;
esac

SECRET_NAME="myblog-secrets"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  MyBlog — Criando Kubernetes Secret"
echo "  Secret    : ${SECRET_NAME}"
echo "  Ambiente  : ${ENVIRONMENT}"
echo "  Namespace : ${NAMESPACE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  Dica: para gerar um JWT_SECRET seguro execute:"
echo "    openssl rand -base64 64 | tr -d '\\n'"
echo ""

# ---------------------------------------------------------------------------
# Solicita os valores sensíveis interativamente.
# read -s: sem eco no terminal — não aparece no histórico de shell.
# ---------------------------------------------------------------------------
read -r -s -p "JWT_SECRET (valor base64 para HS256 — deixe em branco para usar existente): " JWT_SECRET
echo

read -r -s -p "DB_USERNAME (usuário PostgreSQL — ex: myblog_user): " DB_USERNAME
echo

read -r -s -p "DB_PASSWORD (senha PostgreSQL — deixe em branco se não usar): " DB_PASSWORD
echo

# ---------------------------------------------------------------------------
# Valida que JWT_SECRET foi fornecido (obrigatório para a aplicação funcionar)
# ---------------------------------------------------------------------------
if [[ -z "${JWT_SECRET}" ]]; then
  echo ""
  echo "⚠️  JWT_SECRET não informado. O secret não será criado/atualizado."
  echo "   Se o secret já existe no cluster, ele NÃO será modificado."
  kubectl get secret "${SECRET_NAME}" -n "${NAMESPACE}" 2>/dev/null \
    && echo "   ✅ Secret existente encontrado — nenhuma ação necessária." \
    || { echo "   ❌ Secret NÃO encontrado. Forneça o JWT_SECRET para criá-lo."; exit 1; }
  exit 0
fi

# ---------------------------------------------------------------------------
# Cria ou atualiza o secret de forma idempotente:
#   --dry-run=client -o yaml | kubectl apply
#   → cria SE não existe, atualiza SE existe
# ---------------------------------------------------------------------------
echo ""
echo "⏳ Criando/atualizando secret '${SECRET_NAME}' em namespace '${NAMESPACE}'..."

kubectl create secret generic "${SECRET_NAME}" \
  --namespace="${NAMESPACE}" \
  --from-literal="jwt-secret=${JWT_SECRET}" \
  --from-literal="db-username=${DB_USERNAME}" \
  --from-literal="db-password=${DB_PASSWORD}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "✅ Secret '${SECRET_NAME}' criado/atualizado com sucesso em '${NAMESPACE}'."
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Próximos passos:"
echo ""
echo "  1. Confirmar chaves presentes:"
echo "     kubectl describe secret ${SECRET_NAME} -n ${NAMESPACE}"
echo ""
echo "  2. Verificar que os valores não são expostos:"
echo "     kubectl get secret ${SECRET_NAME} -n ${NAMESPACE} -o jsonpath='{.data}'"
echo ""
echo "  3. Reiniciar o deployment para recarregar os secrets:"
echo "     kubectl rollout restart deployment/myblog-api -n ${NAMESPACE}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

