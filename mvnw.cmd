@echo off
setlocal EnableDelayedExpansion

set "MAVEN_VERSION=3.9.6"
set "WRAPPER_DIR=%~dp0.mvn\wrapper"
set "MAVEN_HOME=!WRAPPER_DIR!\apache-maven-!MAVEN_VERSION!"
set "MAVEN_EXEC=!MAVEN_HOME!\bin\mvn.cmd"

if not exist "!MAVEN_EXEC!" (
    echo [mvnw] Apache Maven !MAVEN_VERSION! not found. Downloading...
    if not exist "!WRAPPER_DIR!" mkdir "!WRAPPER_DIR!"

    set "DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/!MAVEN_VERSION!/apache-maven-!MAVEN_VERSION!-bin.zip"
    set "ZIP_FILE=!WRAPPER_DIR!\apache-maven-!MAVEN_VERSION!-bin.zip"

    powershell -NoProfile -ExecutionPolicy Bypass -Command "$url = '!DIST_URL!'; $out = '!ZIP_FILE!'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing"

    if errorlevel 1 (
        echo [mvnw] Download failed. Install Maven manually: https://maven.apache.org/download.cgi
        exit /b 1
    )

    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '!ZIP_FILE!' -DestinationPath '!WRAPPER_DIR!' -Force"
    del "!ZIP_FILE!"
    echo [mvnw] Maven !MAVEN_VERSION! installed at !MAVEN_HOME!
)

if defined JAVA_HOME (
    set "PATH=!JAVA_HOME!\bin;!PATH!"
)

"!MAVEN_EXEC!" %*
endlocal
