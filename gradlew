#!/bin/bash
set -e

# Keep Gradle caches/native state in writable tmp and disable native services
# to avoid host-specific dylib loading failures.
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/tmp/.gradle}"
if [ -n "${GRADLE_OPTS:-}" ]; then
  export GRADLE_OPTS="-Dorg.gradle.native=false ${GRADLE_OPTS}"
else
  export GRADLE_OPTS="-Dorg.gradle.native=false"
fi

if [ -x /tmp/gradle-8.10/bin/gradle ]; then
  exec /tmp/gradle-8.10/bin/gradle "$@"
fi
exec gradle "$@"
