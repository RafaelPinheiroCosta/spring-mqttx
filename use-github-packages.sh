#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -eq 0 ]; then
  echo ""
  echo "Uso:"
  echo "./use-github-packages.sh ./mvnw clean package"
  echo ""
  exit 1
fi

read -p "Digite seu usuario do GitHub: " GITHUB_USER
read -s -p "Digite seu token do GitHub: " GITHUB_TOKEN
echo ""

SETTINGS_PATH="/tmp/github-packages-settings.xml"

cleanup() {
  rm -f "$SETTINGS_PATH"
  echo ""
  echo "Arquivo temporario removido."
}
trap cleanup EXIT

cat > "$SETTINGS_PATH" <<EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>${GITHUB_USER}</username>
      <password>${GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
EOF

echo ""
echo "Executando Maven com autenticacao temporaria do GitHub Packages..."
echo "Arquivo temporario: $SETTINGS_PATH"
echo ""

"$1" -s "$SETTINGS_PATH" "${@:2}"
