#!/bin/bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
rm -rf "$PROJECT_DIR/.m2"
mkdir -p "$PROJECT_DIR/.m2/repository/za/co/common"
MODULES="common-api-starter-parent common-starter-amqp common-starter-api-client common-starter-common common-starter-exception common-starter-http-client common-starter-logging common-starter-metrics common-starter-redis common-starter-security common-starter-sentry common-starter-test common-starter-tracing common-starter-validation common-starter-vault"
for m in $MODULES; do
  S="$HOME/.m2/repository/za/co/common/$m/1.0.0-SNAPSHOT"
  D="$PROJECT_DIR/.m2/repository/za/co/common/$m/1.0.0-SNAPSHOT"
  if [ -d "$S" ]; then
    mkdir -p "$D"
    cp "$S/$m-1.0.0-SNAPSHOT.pom" "$D/" 2>/dev/null || true
    cp "$S/$m-1.0.0-SNAPSHOT.jar" "$D/" 2>/dev/null || true
    echo "  [OK] $m"
  fi
done
echo "Done! .m2 cache ready for: docker compose up --build -d"
