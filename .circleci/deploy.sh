#!/bin/bash
# This script is meant to be run from a Circle CI job to automatically build a new version of the Docker container and
# deploy it to the specified environment.

set -e

ENV=${1?"env is required"}
SERVICE_PATH=${2?"service path is required"}
VERSION=${3?"version is required"}
IAM_ROLE_ARN=
TEMP_LIVE_DIR=/tmp/infrastructure-live

case ${ENV} in
    "prod")
      IAM_ROLE_ARN="arn:aws:iam::060227498266:role/allow-auto-deploy-from-other-accounts"
    ;;
    "stage")
      IAM_ROLE_ARN="arn:aws:iam::023832182978:role/allow-auto-deploy-from-other-accounts"
    ;;
    "dev")
      IAM_ROLE_ARN="arn:aws:iam::429416768433:role/allow-auto-deploy-from-other-accounts"
    ;;
    *)
    echo "unknown $ENV"
    exit 1
    ;;
esac


echo "Deploying $VERSION to $ENV as $SERVICE_PATH @ $IAM_ROLE_ARN"

# All commits will be from the machine user
git config --global user.name "tundra-machine"
git config --global user.email "todor.todorov+machine@tundra.com"
git config --global push.default simple

# change the versions in the infrastructure live repo to the one just pubished
terraform-update-variable --name "image_version" --value "\"$VERSION\"" --vars-path "$SERVICE_PATH/terragrunt.hcl" --git-url "git@github.com:tundracom/infrastructure-live.git" --git-checkout-path "$TEMP_LIVE_DIR"

terragrunt apply --terragrunt-working-dir "$TEMP_LIVE_DIR/$SERVICE_PATH"  --terragrunt-iam-role "$IAM_ROLE_ARN" -input=false -auto-approve

# cleanup local git copy
rm -rf ${TEMP_LIVE_DIR}