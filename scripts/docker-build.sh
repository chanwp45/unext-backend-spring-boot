#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-unext-backend}"
TAG="${TAG:-latest}"
REGISTRY="${REGISTRY:-}"

FULL_IMAGE="${REGISTRY:+${REGISTRY}/}${IMAGE_NAME}:${TAG}"

echo "Building: ${FULL_IMAGE}"
docker build --target runner -t "${FULL_IMAGE}" .
echo "Build complete: ${FULL_IMAGE}"

if [[ "${PUSH:-false}" == "true" ]]; then
  echo "Pushing: ${FULL_IMAGE}"
  docker push "${FULL_IMAGE}"
fi
