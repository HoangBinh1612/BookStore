@echo off
@REM ========================================================
@REM Script tiện ích - Chạy Spring Boot project
@REM Tự động detect JDK 21/22 và dùng Maven đã cài
@REM ========================================================
setlocal

@REM Detect JAVA_HOME
if not "%JAVA_HOME%" == "" goto OkJHome
if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    goto OkJHome
)
if exist "C:\Program Files\Java\jdk-22" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-22"
    goto OkJHome
)
echo Error: Khong tim thay JDK. Hay cai Java JDK 21+
exit /B 1

:OkJHome
echo JAVA_HOME = %JAVA_HOME%

@REM Detect Maven
set "MVN_CMD="
if exist "%USERPROFILE%\.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    set "MVN_CMD=%USERPROFILE%\.maven\apache-maven-3.9.6\bin\mvn.cmd"
    goto OkMvn
)
where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    set "MVN_CMD=mvn"
    goto OkMvn
)
echo Error: Khong tim thay Maven.
exit /B 1

:OkMvn
echo Maven = %MVN_CMD%
echo ========================================
echo   Dang khoi dong ung dung...
echo   Truy cap: http://localhost:8080
echo ========================================

"%MVN_CMD%" spring-boot:run
