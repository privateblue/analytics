#!/usr/bin/env bash

export JAVA_OPTS="\
-Dcom.tundra.cr.config.dynamoPrefix=dev \
-Dcom.tundra.cr.config.recreateTables=false \
"

sbt "catalog-recommendations-service/runMain com.tundra.cr.Server \
-http.port=:7400 \
-admin.port=:7402
"
