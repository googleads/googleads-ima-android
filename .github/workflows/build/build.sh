#!/bin/bash

set -eo pipefail
projects=("${REGEX}")

for PROJ_DIR in "${projects[@]}"
do
  CHANGES="$(git --no-pager diff --name-only "${COMMIT_RANGE}")";
  echo "Commit range: ${COMMIT_RANGE}; Project dir: ${PROJ_DIR};\n Changes: ${CHANGES}";
  if [[ -n "$(grep -E "(${PROJ_DIR}|\.github\/workflows)" <<< "${CHANGES}")" ]]; then
    echo "Building for ${PROJ_DIR}";
    pushd "$PROJ_DIR";
    ./gradlew build;
    popd;
  fi
done
