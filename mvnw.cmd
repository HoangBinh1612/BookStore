@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script (simplified for HoangDuongBinhProject)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

@REM Detect JAVA_HOME
if not "%JAVA_HOME%" == "" goto OkJHome
@REM Auto-detect Java installation
if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    goto OkJHome
)
if exist "C:\Program Files\Java\jdk-22" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-22"
    goto OkJHome
)
echo Error: JAVA_HOME not found. Please set JAVA_HOME.
exit /B 1

:OkJHome
set "MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe"

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM Download maven-wrapper.jar if not exists
if exist "%WRAPPER_JAR%" goto runWrapper
echo Downloading Maven Wrapper...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"

:runWrapper
"%MAVEN_JAVA_EXE%" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

exit /B %ERRORLEVEL%
