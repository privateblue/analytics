#!/usr/bin/env bash

export JAVA_OPTS=""

sbt "analytics-service/runMain com.tundra.analytics.Server \
-http.port=:7400 \
-admin.port=:7402
"
