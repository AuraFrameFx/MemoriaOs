@echo off
echo ================================================
echo GenesisOS Build Environment Diagnostic Tool
echo ================================================
echo.

echo Checking Java installation...
echo.

REM Check if java command is available
echo Testing java command:
java -version 2>NUL
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: 'java' command not found in PATH
    echo This is likely the cause of CreateProcess error=2
) else (
    echo ✅ Java command found in PATH
)

echo.
echo Testing javac command:
javac -version 2>NUL
if %ERRORLEVEL% NEQ 0 (
    echo ❌ WARNING: 'javac' not found - JDK may not be installed
) else (
    echo ✅ javac (Java compiler) found
)

echo.
echo Checking JAVA_HOME environment variable:
if "%JAVA_HOME%"=="" (
    echo ❌ ERROR: JAVA_HOME is not set
) else (
    echo ✅ JAVA_HOME is set to: %JAVA_HOME%
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo ✅ java.exe found in JAVA_HOME\bin
    ) else (
        echo ❌ ERROR: java.exe not found in JAVA_HOME\bin
    )
)

echo.
echo Checking system PATH for Java:
echo %PATH% | findstr /i "java" >NUL
if %ERRORLEVEL% EQU 0 (
    echo ✅ Java directory found in PATH
) else (
    echo ❌ ERROR: No Java directory in PATH
)

echo.
echo ================================================
echo DIAGNOSIS COMPLETE
echo ================================================
echo.

REM Determine the specific issue
if "%JAVA_HOME%"=="" (
    if not java -version >NUL 2>&1 (
        echo 🔍 PROBLEM IDENTIFIED: Java is not installed or not in PATH
        echo.
        echo 🚀 SOLUTIONS:
        echo.
        echo Option 1 - Install Java JDK 21^+:
        echo 1. Download from: https://adoptium.net/
        echo 2. Install Eclipse Temurin JDK 21
        echo 3. During installation, check "Set JAVA_HOME" and "Add to PATH"
        echo 4. Restart command prompt
        echo.
        echo Option 2 - Use existing Java installation:
        echo 1. Find your Java installation directory
        echo 2. Set JAVA_HOME: setx JAVA_HOME "C:\path\to\java"
        echo 3. Add to PATH: setx PATH "%%PATH%%;%%JAVA_HOME%%\bin"
        echo 4. Restart command prompt
        echo.
        echo Option 3 - Check Windows Programs:
        echo 1. Go to Settings ^> Apps ^> Installed apps
        echo 2. Search for "Java" or "JDK"
        echo 3. Note the installation directory
        echo 4. Set environment variables manually
    )
)

echo.
echo Testing Gradle wrapper after diagnosis:
gradlew --version
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Gradle wrapper still failing
    echo.
    echo MANUAL FIX COMMANDS:
    echo.
    echo REM Set JAVA_HOME ^(replace with your Java path^)
    echo setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot"
    echo.
    echo REM Add Java to PATH
    echo setx PATH "%%PATH%%;%%JAVA_HOME%%\bin"
    echo.
    echo REM Restart command prompt and try again
    echo gradlew --version
) else (
    echo ✅ Gradle wrapper working correctly!
)

echo.
pause
