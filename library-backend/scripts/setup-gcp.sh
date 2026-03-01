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
  compute.googleapis.com \
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
# Roles para crear/gestionar la VM y empujar imágenes a Artifact Registry.
# NO se necesitan roles de SSH/IAP porque el deploy es por metadata de instancia.
for ROLE in \
  roles/compute.instanceAdmin.v1 \
  roles/iam.serviceAccountUser \
  roles/artifactregistry.admin \
  roles/compute.securityAdmin; do
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="${ROLE}" --condition=None --quiet
done

echo "▶ [4/5] Otorgando a la SA por defecto de Compute Engine acceso de lectura a Artifact Registry..."
# La VM usa la SA por defecto ({PROJECT_NUMBER}-compute@developer.gserviceaccount.com)
# con scope cloud-platform para hacer docker pull desde Artifact Registry.
PROJECT_NUMBER=$(gcloud projects describe "${PROJECT_ID}" --format='value(projectNumber)')
COMPUTE_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member="serviceAccount:${COMPUTE_SA}" \
  --role="roles/artifactregistry.reader" \
  --condition=None --quiet

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
echo " VM_NAME        = library-backend"
echo " VM_ZONE        = us-central1-a"
echo "════════════════════════════════════════════════════════════"
echo "⚠️  No subas '${KEY_FILE}' al repositorio."
