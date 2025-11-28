@echo off
setlocal

set "WRAPPER_JAR=%CD%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar"

if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    if not exist "%CD%\.mvn\wrapper" mkdir "%CD%\.mvn\wrapper"
    powershell -Command "& {[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')}"
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to download Maven Wrapper.
        exit /b 1
    )
    echo Maven Wrapper downloaded successfully.
) else (
    echo Maven Wrapper already exists.
)

endlocal
