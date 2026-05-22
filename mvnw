#!/bin/sh
# Maven Wrapper — downloads Apache Maven 3.9.6 on first run

MAVEN_VERSION="3.9.6"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$SCRIPT_DIR/.mvn/wrapper"
MAVEN_HOME="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION"
MAVEN_EXEC="$MAVEN_HOME/bin/mvn"

if [ ! -f "$MAVEN_EXEC" ]; then
    echo "[mvnw] Apache Maven $MAVEN_VERSION not found. Downloading..."
    DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz"
    ARCHIVE="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"

    mkdir -p "$WRAPPER_DIR"

    if command -v curl >/dev/null 2>&1; then
        curl -fL -o "$ARCHIVE" "$DOWNLOAD_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$ARCHIVE" "$DOWNLOAD_URL"
    else
        echo "[mvnw] Neither curl nor wget found. Please install Maven manually."
        exit 1
    fi

    tar xzf "$ARCHIVE" -C "$WRAPPER_DIR"
    rm -f "$ARCHIVE"
    chmod +x "$MAVEN_EXEC"
    echo "[mvnw] Maven $MAVEN_VERSION installed at $MAVEN_HOME"
fi

exec "$MAVEN_EXEC" "$@"
