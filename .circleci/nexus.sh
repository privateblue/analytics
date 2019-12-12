#!/usr/bin/env bash

set -e

if [ -z "$NEXUS_PASSWORD" ]; then
  echo "NEXUS_PASSWORD environment variable must be set"
  exit 1
fi

echo "
realm=Sonatype Nexus Repository Manager
host=nexus.tundra-shared.com
user=reader
password=${NEXUS_PASSWORD}
"  > ~/.ivy2/.credentials
