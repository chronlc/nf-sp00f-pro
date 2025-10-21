#!/bin/bash

# Gradle wrapper script for Linux
# This script downloads and runs Gradle if not present

set -e

# Get the directory of this script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Gradle version
GRADLE_VERSION="8.5"
GRADLE_DIST="gradle-${GRADLE_VERSION}-all"
GRADLE_URL="https://services.gradle.org/distributions/${GRADLE_DIST}.zip"
GRADLE_HOME="${DIR}/gradle-${GRADLE_VERSION}"

# Check if gradle is already downloaded
if [ ! -d "$GRADLE_HOME" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "$DIR/.gradle-download"
    cd "$DIR/.gradle-download"
    
    if command -v wget &> /dev/null; then
        wget -q "$GRADLE_URL"
    elif command -v curl &> /dev/null; then
        curl -s -O "$GRADLE_URL"
    else
        echo "Error: wget or curl required to download Gradle"
        exit 1
    fi
    
    echo "Extracting Gradle..."
    unzip -q "$GRADLE_DIST.zip"
    mv "$GRADLE_DIST" "$GRADLE_HOME"
    cd "$DIR"
    rm -rf "$DIR/.gradle-download"
    echo "Gradle installed successfully"
fi

# Run gradle with passed arguments
"$GRADLE_HOME/bin/gradle" "$@"
