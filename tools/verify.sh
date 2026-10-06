#!/usr/bin/env bash
# Everything that can be checked without the Android SDK: the content build and
# the shared tests (search, content integrity, Quran asset).
#
#   tools/verify.sh /path/to/quran-json-package
set -euo pipefail

cd "$(dirname "$0")/.."
DATASET="${1:-}"

if [[ -n "$DATASET" ]]; then
    python3 tools/build_content.py --dataset "$DATASET"
else
    echo "No dataset given; keeping the assets already in androidApp/src/main/assets/content."
fi

./gradlew :shared:testDebugUnitTest
