#!/bin/bash
# vm-startup.sh — ejecutado por GCE en cada arranque de la VM.
# NO requiere SSH. El workflow solo actualiza la metadata 'docker-image'
# y este script (o el timer) la observa y redespliega el contenedor.
set -euo pipefail

DEPLOY_SCRIPT=/var/lib/library-deploy.sh
DATA_DIR=/mnt/stateful_partition/library/data
STATE_FILE=/mnt/stateful_partition/library/.deployed-image

mkdir -p "${DATA_DIR}"

# ---------- helper de despliegue (reutilizado por el timer) ----------
cat > "${DEPLOY_SCRIPT}" << 'DEPLOY_EOF'
#!/bin/bash
set -euo pipefail

METADATA_URL=http://metadata.google.internal/computeMetadata/v1/instance/attributes
DATA_DIR=/mnt/stateful_partition/library/data
STATE_FILE=/mnt/stateful_partition/library/.deployed-image

log() { logger -t library-deploy -- "$*"; echo "[library-deploy] $*"; }

TARGET=$(curl -sf "${METADATA_URL}/docker-image" \
  -H "Metadata-Flavor: Google" 2>/dev/null || echo "")

[ -z "${TARGET}" ] && { log "docker-image metadata no establecida aun."; exit 0; }

CURRENT=$(cat "${STATE_FILE}" 2>/dev/null || echo "")
[ "${TARGET}" = "${CURRENT}" ] && exit 0

log "Desplegando imagen: ${TARGET}"
mkdir -p "${DATA_DIR}"

# Autenticar Docker en Artifact Registry con el token de la SA de la VM
# (sin gcloud, sin SSH, sin credenciales hardcodeadas)
ACCESS_TOKEN=$(curl -sf \
  "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token" \
  -H "Metadata-Flavor: Google" \
  | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

echo "${ACCESS_TOKEN}" | docker login \
  -u oauth2accesstoken --password-stdin us-central1-docker.pkg.dev \
  2>&1 | logger -t library-deploy

docker pull "${TARGET}" 2>&1 | logger -t library-deploy

docker stop library-backend 2>/dev/null || true
docker rm   library-backend 2>/dev/null || true

docker run -d \
  --name library-backend \
  --restart=always \
  -p 8080:8080 \
  -e PORT=8080 \
  -e DB_PATH=/data/library.db \
  -v "${DATA_DIR}:/data" \
  "${TARGET}" 2>&1 | logger -t library-deploy

echo "${TARGET}" > "${STATE_FILE}"
log "Despliegue exitoso: ${TARGET}"
DEPLOY_EOF

chmod +x "${DEPLOY_SCRIPT}"

# ---------- systemd: service ----------
cat > /etc/systemd/system/library-deploy.service << 'SVC_EOF'
[Unit]
Description=Deploy library-backend from GCE instance metadata
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/var/lib/library-deploy.sh
StandardOutput=journal
StandardError=journal
SVC_EOF

# ---------- systemd: timer (cada 60 s) ----------
cat > /etc/systemd/system/library-deploy.timer << 'TIMER_EOF'
[Unit]
Description=Polling de metadata GCE para redeploy de library-backend

[Timer]
OnBootSec=30s
OnUnitActiveSec=60s

[Install]
WantedBy=timers.target
TIMER_EOF

systemctl daemon-reload
systemctl enable library-deploy.timer
systemctl start library-deploy.timer

# Primer despliegue (si la metadata 'docker-image' ya está presente)
echo "[library-startup] Verificando primer despliegue..."
"${DEPLOY_SCRIPT}" || echo "[library-startup] Sin imagen en metadata aun; el timer lo reintentara."

echo "[library-startup] Startup completo. Timer activo (cada 60 s)."
