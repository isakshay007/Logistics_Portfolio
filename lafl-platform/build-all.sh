#!/bin/bash
set -e
cd "$(dirname "$0")"
../gradlew bootJar -x test
echo "All jars built successfully"
