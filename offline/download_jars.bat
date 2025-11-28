@echo off
setlocal enabledelayedexpansion

echo Downloading required JAR files...

set "JARS=(
    "https://repo1.maven.org/maven2/com/google/api-client/google-api-client/2.2.0/google-api-client-2.2.0.jar"
    "https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client/1.34.1/google-oauth-client-1.34.1.jar"
    "https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client-jetty/1.34.1/google-oauth-client-jetty-1.34.1.jar"
    "https://repo1.maven.org/maven2/com/google/http-client/google-http-client/1.42.3/google-http-client-1.42.3.jar"
    "https://repo1.maven.org/maven2/com/google/http-client/google-http-client-jackson2/1.42.3/google-http-client-jackson2-1.42.3.jar"
    "https://repo1.maven.org/maven2/com/google/api-client/google-api-client-gson/2.2.0/google-api-client-gson-2.2.0.jar"
    "https://repo1.maven.org/maven2/com/google/apis/google-api-services-drive/v3-rev20230507-2.0.0/google-api-services-drive-v3-rev20230507-2.0.0.jar"
    "https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.19.0/google-auth-library-oauth2-http-1.19.0.jar"
    "https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.19.0/google-auth-library-credentials-1.19.0.jar"
    "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.13.4/jackson-core-2.13.4.jar"
    "https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar"
    "https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar"
    "https://repo1.maven.org/maven2/commons-codec/commons-codec/1.15/commons-codec-1.15.jar"
    "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/9.4.44.v20210927/jetty-server-9.4.44.v20210927.jar"
    "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/9.4.44.v20210927/jetty-util-9.4.44.v20210927.jar"
    "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/9.4.44.v20210927/jetty-io-9.4.44.v20210927.jar"
    "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/9.4.44.v20210927/jetty-http-9.4.44.v20210927.jar"
)"

if not exist "lib" mkdir lib

for %%j in (%JARS%) do (
    echo Downloading %%~nxj...
    powershell -Command "& {[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%%j', 'lib\%%~nxj')"}
    if !ERRORLEVEL! NEQ 0 (
        echo Failed to download %%~nxj
        pause
        exit /b 1
    )
)

echo.
echo All JAR files downloaded successfully to the 'lib' folder.
echo.
echo To compile and run your project, use the following commands:
echo.
echo   javac -cp ".;lib\*" src\*.java -d out
echo   java -cp ".;out;lib\*" Main
echo.
pause
