#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Install JDK 25 first." >&2
  exit 1
fi
if ! java -version 2>&1 | grep -q '25\.'; then
  echo "ERROR: This project requires Java 25." >&2
  java -version >&2
  exit 1
fi
if ! command -v gradle >/dev/null 2>&1; then
  echo "ERROR: Install Gradle 9.7.1 or use the GitHub Actions build workflow." >&2
  exit 1
fi
gradle --no-daemon clean build
printf '\nBuild succeeded. Check build/libs for the mod JAR.\n'
