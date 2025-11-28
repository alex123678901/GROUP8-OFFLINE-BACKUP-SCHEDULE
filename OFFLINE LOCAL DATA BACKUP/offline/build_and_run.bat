@echo off
setlocal

REM Check if required JARs exist
if not exist "lib" (
    echo Required JAR files not found. Please run download_jars.bat first.
    pause
    exit /b 1
)

REM Create output directory if it doesn't exist
if not exist "out" mkdir out

echo Compiling Java files...
javac -cp ".;lib\*" src\*.java -d out

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed. Please check for errors.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Compilation successful! Running the application...
echo.

REM Run the application
java -cp ".;out;lib\*" Main

pause

endlocal
