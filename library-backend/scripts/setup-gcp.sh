#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# setup-gcp.sh  –  Configuración ONE-TIME antes del primer despliegue.
#
# USO:
#   chmod +x library-backend/scripts/setup-gcp.sh
#   GCP_PROJECT_ID=mi-proyecto ./library-backend/scripts/setup-gcp.sh
# ──────────────────────────────────────────────────────────────────────────────
set -euo pipefail

PROJECT_ID="${GCP_PROJECT_ID:?Define GCP_PROJECT_ID}"
SA_NAME="github-actions-deployer"
KEY_FILE="gcp-sa-key.json"

echo "▶ [1/4] Activando APIs..."
gcloud services enable compute.googleapis.com artifactregistry.googleapis.com iam.googleapis.com \
  --project="${PROJECT_ID}"

echo "▶ [2/4] Creando Service Account..."
gcloud iam service-accounts describe "${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
  --project="${PROJECT_ID}" 2>/dev/null \
|| gcloud iam service-accounts create "${SA_NAME}" \
    --project="${PROJECT_ID}" --display-name="GitHub Actions Deployer"

echo "▶ [3/4] Asignando roles IAM..."
for ROLE in roles/compute.instanceAdmin.v1 roles/iam.serviceAccountUser \
            roles/artifactregistry.writer roles/compute.securityAdmin; do
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="${ROLE}" --condition=None --quiet
done

echo "▶ [4/4] Generando clave JSON..."
gcloud iam service-accounts keys create "${KEY_FILE}" \
  --iam-account="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
  --project="${PROJECT_ID}"

echo ""
echo "════════════════════════════════════════════════════════════"
echo " Agrega estos Secrets en GitHub:"
echo " https://github.com/maldos23/libary-app/settings/secrets/actions"
echo ""
echo " GCP_PROJECT_ID = ${PROJECT_ID}"
echo " GCP_SA_KEY     = \$(cat ${KEY_FILE} | base64 | tr -d '\\n')"
echo " VM_NAME        = library-backend"
echo " VM_ZONE        = us-central1-a"
echo "════════════════════════════════════════════════════════════"
echo "⚠️  No subas '${KEY_FILE}' al repositorio."
