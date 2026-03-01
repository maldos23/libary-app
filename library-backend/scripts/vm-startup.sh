#!/bin/bash
# vm-startup.sh — ejecutado por GCE en cada arranque (Debian 12).
# NO requiere SSH. El workflow actualiza la metadata 'docker-image'
# y el systemd timer lo detecta y redespliega el contenedor.
# Sistema: debian-12 (debian-cloud) — filesystem completamente escribible.

exec > >(tee /var/log/library-startup.log | logger -t library-startup) 2>&1
echo "[startup] Iniciando $(date -u)"

DEPLOY_SCRIPT=/opt/library-deploy.sh
DATA_DIR=/var/lib/library-data

# ── 1. Instalar Docker CE si no esta presente ─────────────────────────────
if ! command -v docker &>/dev/null; then
  echo "[startup] Instalando Docker CE..."
  apt-get update -qq
  apt-get install -y -qq ca-certificates curl gnupg
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/debian/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  CODENAME=$(. /etc/os-release && echo "${VERSION_CODENAME}")
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian ${CODENAME} stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -qq
  apt-get install -y -qq docker-ce docker-ce-cli containerd.io
  systemctl enable --now docker
  echo "[startup] Docker instalado: $(docker --version)"
else
  echo "[startup] Docker ya presente: $(docker --version)"
fi

mkdir -p "${DATA_DIR}"

# ── 2. Script de despliegue ─────────────────────────────────────────────
cat > "${DEPLOY_SCRIPT}" << 'DEPLOY_EOF'
#!/bin/bash
set -euo pipefail
METADATA=http://metadata.google.internal/computeMetadata/v1/instance/attributes
DATA_DIR=/var/lib/library-data
STATE_FILE=/var/lib/library-data/.deployed-image
log() { logger -t library-deploy -- "$*"; echo "[deploy] $*"; }

TARGET=$(curl -sf "${METADATA}/docker-image" -H "Metadata-Flavor: Google" 2>/dev/null || true)
[ -z "${TARGET}" ] && { log "metadata docker-image vacia."; exit 0; }

CURRENT=$(cat "${STATE_FILE}" 2>/dev/null || true)
[ "${TARGET}" = "${CURRENT}" ] && { log "Sin cambios (${TARGET})."; exit 0; }
log "Nueva imagen: ${TARGET}"

TOKEN=$(curl -sf \
  "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token" \
  -H "Metadata-Flavor: Google" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")
echo "${TOKEN}" | docker login -u oauth2accesstoken --password-stdin us-central1-docker.pkg.dev

docker pull "${TARGET}"
docker stop  library-backend 2>/dev/null || true
docker rm    library-backend 2>/dev/null || true
docker run -d \
  --name library-backend \
  --restart=always \
  -p 8080:8080 \
  -e PORT=8080 \
  -e DB_PATH=/data/library.db \
  -v "${DATA_DIR}:/data" \
  "${TARGET}"
echo "${TARGET}" > "${STATE_FILE}"
log "Despliegue OK: ${TARGET}"
DEPLOY_EOF

chmod +x "${DEPLOY_SCRIPT}"

# ── 3. Systemd service ─────────────────────────────────────────────
cat > /etc/systemd/system/library-deploy.service << 'SVC'
[Unit]
Description=Redeploy library-backend from GCE metadata
After=docker.service network-online.target
Wants=network-online.target docker.service
[Service]
Type=oneshot
ExecStart=/opt/library-deploy.sh
StandardOutput=journal
StandardError=journal
SVC

# ── 4. Systemd timer (cada 60 s) ──────────────────────────────────────
cat > /etc/systemd/system/library-deploy.timer << 'TIMER'
[Unit]
Description=Polling metadata GCE para redeploy library-backend
[Timer]
OnBootSec=20s
OnUnitActiveSec=60s
[Install]
WantedBy=timers.target
TIMER

systemctl daemon-reload
systemctl enable library-deploy.timer
systemctl start  library-deploy.timer

# ── 5. Primer despliegue inmediato ──────────────────────────────────
echo "[startup] Ejecutando primer despliegue..."
"${DEPLOY_SCRIPT}" \
  && echo "[startup] Primer despliegue OK." \
  || echo "[startup] Fallo; el timer reintentara en 60 s."

echo "[startup] Listo $(date -u)"
