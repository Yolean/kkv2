#!/usr/bin/env bash
[ -z "$DEBUG" ] || set -x
set -eo pipefail

# Experimental
# https://quarkus.io/guides/maven-tooling#remote-development-mode

MVNFLAGS=""
[ -z "$DEBUG" ] || MVNFLAGS="-X"

echo "=> Building mutating-jar at target/kkv2-dev.jar"

mvn $MVNFLAGS quarkus:remote-dev \
  -Dquarkus.package.type=mutable-jar \
  -Dquarkus.container-image.build=false
