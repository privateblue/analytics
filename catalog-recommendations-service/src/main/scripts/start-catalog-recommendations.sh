#!/usr/bin/env bash

export JAVA_OPTS=""

sbt "catalog-recommendations-service/runMain com.tundra.cr.Server \
-http.port=:7400 \
-admin.port=:7402
"
