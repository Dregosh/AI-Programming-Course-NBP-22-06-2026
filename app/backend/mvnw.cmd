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

@REM Apache Maven Wrapper startup batch script, version 3.3.2

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __ MVNW_CMD__=%0
@SET __MVNW_ERROR__=
@SET __MVNW_REPOURL__=

@SET MAVEN_WRAPPER_DIR=%~dp0.mvn\wrapper
@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_WRAPPER_DIR%\maven-wrapper.properties

@IF NOT EXIST "%MAVEN_WRAPPER_PROPERTIES%" (
    ECHO Cannot find .mvn\wrapper\maven-wrapper.properties
    EXIT /B 1
)

@FOR /F "usebackq tokens=1,* delims==" %%a IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    IF "%%a"=="distributionUrl" SET DISTRIBUTION_URL=%%b
)

@SET MAVEN_USER_HOME=%USERPROFILE%\.m2\wrapper
@FOR %%F IN ("%DISTRIBUTION_URL%") DO @SET DIST_FILENAME=%%~nF
@SET MAVEN_HOME=%MAVEN_USER_HOME%\dists\%DIST_FILENAME%

@IF NOT EXIST "%MAVEN_HOME%" (
    ECHO Downloading Apache Maven...
    MKDIR "%MAVEN_HOME%"
    powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_HOME%\maven.zip'"
    powershell -Command "Expand-Archive -Path '%MAVEN_HOME%\maven.zip' -DestinationPath '%MAVEN_HOME%'"
    DEL "%MAVEN_HOME%\maven.zip"
)

@SET MVN_CMD=
@FOR /R "%MAVEN_HOME%" %%F IN (mvn.cmd) DO (
    IF NOT DEFINED MVN_CMD SET MVN_CMD=%%F
)

@IF NOT DEFINED MVN_CMD (
    ECHO ERROR: Could not find mvn.cmd in %MAVEN_HOME%
    EXIT /B 1
)

@"%MVN_CMD%" %*
