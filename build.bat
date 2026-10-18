@echo off
setlocal
cd /d "%~dp0"
echo ======================================
echo   All-Kotoff-Want build helper
echo ======================================
where java >nul 2>nul || (echo ERROR: Install JDK 25 first.& pause & exit /b 1)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%~v
echo Detected Java: %JAVA_VERSION%
java -version 2>&1 | findstr /c:"25." >nul || (echo ERROR: This project needs Java 25.& pause & exit /b 1)
where gradle >nul 2>nul || (echo ERROR: Install Gradle 9.7.1 and add it to PATH, or use the GitHub Actions build workflow.& pause & exit /b 1)
gradle --no-daemon clean build
if errorlevel 1 (
  echo BUILD FAILED. Copy the full error log if you need help fixing it.
  pause
  exit /b 1
)
echo.
echo BUILD SUCCEEDED. Check build\libs for the mod JAR.
pause
