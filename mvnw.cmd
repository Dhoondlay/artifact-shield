@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM   https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Maven Wrapper (Windows)
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET PN=%__MVNW_ARG0_NAME__%
@SET "[email protected]"
@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%"\.mvn goto baseDirFound
@cd ..
@IF "%WDIR%"=="%CD%" goto baseDirNotFound
@SET "WDIR=%CD%"
@goto findBaseDir

:baseDirFound
@SET "MAVEN_PROJECTBASEDIR=%WDIR%"
@cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
@SET "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
@cd "%EXEC_DIR%"

:endDetectBaseDir
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir2
@SET "MAVEN_PROJECTBASEDIR=%CD%"
:endDetectBaseDir2

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@IF EXIST %WRAPPER_JAR% (
    goto runMavenWithJavaHome
)

@IF NOT "%MVNW_REPOURL%" == "" (
    SET DOWNLOAD_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
)

@IF "%MVNW_VERBOSE%"=="true" (
  @echo Downloading from: %DOWNLOAD_URL%
)

@powershell -Command "&{"^
    "$webclient = new-object System.Net.WebClient;"^
    "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
    "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
    "}"^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', %WRAPPER_JAR%)"^
    "}"
@IF "%MVNW_VERBOSE%"=="true" (
  @echo Finished downloading %WRAPPER_JAR%
)

:runMavenWithJavaHome
@IF NOT "%JAVA_HOME%"=="" (
  SET JAVACMD="%JAVA_HOME%/bin/java.exe"
  goto init
)

@SET JAVACMD=java.exe

:init
%JAVACMD% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
