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

echo "▶ [1/5] Activando APIs..."
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  iam.googleapis.com \
  cloudresourcemanager.googleapis.com \
  --project="${PROJECT_ID}"

echo "▶ [2/5] Creando Service Account del workflow (GitHub Actions)..."
gcloud iam service-accounts describe "${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
  --project="${PROJECT_ID}" 2>/dev/null \
|| gcloud iam service-accounts create "${SA_NAME}" \
    --project="${PROJECT_ID}" --display-name="GitHub Actions Deployer"

echo "▶ [3/5] Asignando roles al SA del workflow..."
# Cloud Run Admin: crear/actualizar servicios
# Artifact Registry Admin: crear repo y push de imagenes
# Service Account User: para que Cloud Run pueda invocar la SA por defecto
for ROLE in \
  roles/run.admin \
  roles/artifactregistry.admin \
  roles/iam.serviceAccountUser; do
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="${ROLE}" --condition=None --quiet
done

echo "▶ [4/5] Permitiendo invocaciones publicas en Cloud Run..."
# Necesario para que la API sea accesible sin autenticacion
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member="allUsers" \
  --role="roles/run.invoker" \
  --condition=None --quiet 2>/dev/null \
  || echo "  (Esta politica puede requerir permisos de org — omitida)"

echo "▶ [5/5] Generando clave JSON..."
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
echo "════════════════════════════════════════════════════════════"
echo "⚠️  No subas '${KEY_FILE}' al repositorio."
echo ""
echo " Ya no se necesitan VM_NAME ni VM_ZONE (Cloud Run no usa VMs)."
