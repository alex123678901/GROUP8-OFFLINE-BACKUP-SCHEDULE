@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM M2_HOME - location of maven2's installed home dir
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%" == "" goto skipRcPre
@REM check for pre script, once with legacy .bat ending and once with .cmd ending
if exist "%USERPROFILE%\mavenrc_pre.bat" call "%USERPROFILE%\mavenrc_pre.bat" %*
if exist "%USERPROFILE%\mavenrc_pre.cmd" call "%USERPROFILE%\mavenrc_pre.cmd" %*
:skipRcPre

@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@if "%M2_HOME%"=="" goto noM2Home
@if not "%M2_HOME:\"=="%M2_HOME%" goto noM2Home
@set "M2_HOME=%M2_HOME;\=/%"
@set "M2_HOME=%M2_HOME; =/%"
@set "M2_HOME=%M2_HOME%"

@if "%MAVEN_PROJECTBASEDIR%"=="" goto noProjectBaseDir
@if not "%MAVEN_PROJECTBASEDIR:\"=="%MAVEN_PROJECTBASEDIR%" goto noProjectBaseDir
@set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR;\=/%"
@set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR; =/%"
@set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%"

@REM Find the project base dir, looking up the parent directories, until the .mvn directory is found.
@REM If not found, use the directory from which the script has been called
:findProjectBaseDir
@if exist "%MAVEN_PROJECTBASEDIR%\.mvn" goto foundProjectBaseDir
@set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%\..
@if "%MAVEN_PROJECTBASEDIR%"=="" goto noProjectBaseDir
@if "%MAVEN_PROJECTBASEDIR%"=="%CD%" goto noProjectBaseDir
@cd /d "%MAVEN_PROJECTBASEDIR%"
@set "MAVEN_PROJECTBASEDIR=%CD%"
@goto findProjectBaseDir
:noProjectBaseDir
@set MAVEN_PROJECTBASEDIR=%CD%
:foundProjectBaseDir

@REM Maven version
@set MAVEN_VERSION=3.8.6

@REM Look for a '.mvn' directory to detect if we're running in a multi-module project
@if exist "%MAVEN_PROJECTBASEDIR%\.mvn" (
  @set MAVEN_PROJECTBASEDIR_DETECTED=true
) else (
  @set MAVEN_PROJECTBASEDIR_DETECTED=false
)

@if "%MAVEN_SKIP_RC%" == "" (
  @REM Check for project based .mvn directory and maven config
  @if "%MAVEN_PROJECTBASEDIR_DETECTED%" == "true" (
    @if exist "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" (
      @setlocal EnableDelayedExpansion
      @set jvmConfig=""
      @for /f "usebackq delims=" %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config") do @set jvmConfig=!jvmConfig! %%a
      @endlocal & set MAVEN_PROJECT_JVM_OPTS=%jvmConfig%
    )
    @if exist "%MAVEN_PROJECTBASEDIR%\.mvn\maven.config" (
      @setlocal EnableDelayedExpansion
      @set mavenConfig=""
      @for /f "usebackq delims=$" %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\maven.config") do @set mavenConfig=!mavenConfig! %%a
      @endlocal & set MAVEN_CONFIG=%mavenConfig%
    )
  )
  @REM Check for user .m2 directory and maven config
  @if exist "%USERPROFILE%\.m2\maven.config" (
    @setlocal EnableDelayedExpansion
    @set userMavenConfig=""
    @for /f "usebackq delims=$" %%a in ("%USERPROFILE%\.m2\maven.config") do @set userMavenConfig=!userMavenConfig! %%a
    @endlocal & set MAVEN_CONFIG=%userMavenConfig%
  )
)

@REM Set up the default classpath to include the Maven wrapper jar
@set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

@if not exist "%WRAPPER_JAR%" (
  @if "%MVNW_VERBOSE%" == "true" (
    echo Couldn't find %WRAPPER_JAR%, downloading it ...
  )
  @if "%MVNW_VERBOSE%" == "true" (
    echo Downloading from: %MAVEN_WRAPPER_URL%
  )
  @set MAVEN_WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
  @for /f "tokens=1,2 delims=: " %%A in ('findstr /I /C:"Maven" "%MAVEN_PROJECTBASEDIR%\pom.xml"') do (
    @if "%%A"=="<maven.wrapper.version>" set MAVEN_WRAPPER_VERSION=%%B
  )
  @if "%MAVEN_WRAPPER_VERSION%"=="" set MAVEN_WRAPPER_VERSION=3.1.0
  @set MAVEN_WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/%MAVEN_WRAPPER_VERSION%/maven-wrapper-%MAVEN_WRAPPER_VERSION%.jar
  @for /f "tokens=*" %%i in ('powershell -Command "& {[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%MAVEN_WRAPPER_URL%', '%WRAPPER_JAR%')}"') do set DOWNLOAD_RESULT=%%i
  @if not exist "%WRAPPER_JAR%" (
    echo Could not download Maven wrapper from %MAVEN_WRAPPER_URL% to %WRAPPER_JAR%
    echo Please download the jar manually and place it at %WRAPPER_JAR%
    echo See https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html for more information
    @goto error
  )
)

@REM Find the Java executable
@set JAVA_EXE=java
@if "%JAVA_HOME%" != "" (
  @set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)
@if not exist "%JAVA_EXE%" (
  @echo Error: JAVA_HOME is not set or invalid.
  @echo Please set the JAVA_HOME variable in your environment to match the
  @echo location of your Java installation.
  @goto error
)

@REM Configure Maven to use the maximum available memory
@set MAVEN_OPTS=%MAVEN_OPTS% -Xmx1024m

@REM Execute Maven
"%JAVA_EXE%" %JVM_CONFIG_MAVEN_PROPS% %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

:end
@endlocal

@if "%MAVEN_BATCH_PAUSE%" == "on" pause

@if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
@endlocal
@set ERROR_CODE=1

goto end

:noM2Home
@echo Error: M2_HOME is not set or invalid.
@echo Please set the M2_HOME variable in your environment to match the
@echo location of the Maven installation.
@set ERROR_CODE=1
goto error
