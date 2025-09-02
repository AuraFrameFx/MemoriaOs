@echo off
echo ===============================================
echo Comprehensive Build Environment Diagnostic
echo ===============================================

echo Step 1: Testing Java Environment
echo ---------------------------------
set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=%PATH%;C:\Program Files\Java\jdk-24\bin

echo Testing direct Java execution:
"C:\Program Files\Java\jdk-24\bin\java.exe" -version
echo Java test result: %ERRORLEVEL%

echo.
echo Step 2: Testing Gradle with Verbose Output
echo ------------------------------------------
echo Running gradlew --version with detailed logging...
gradlew --version --info --stacktrace 2>&1

echo.
echo Step 3: Testing Gradle Clean with Maximum Detail
echo ------------------------------------------------
echo Running gradlew clean with full diagnostic output...
gradlew clean --info --stacktrace --debug 2>&1

echo.
echo Step 4: System Environment Check
echo --------------------------------
echo Current JAVA_HOME: %JAVA_HOME%
echo Current PATH (Java portions):
echo %PATH% | findstr /i "java"

echo.
echo Step 5: Android SDK Check
echo ------------------------
if exist "%ANDROID_HOME%" (
    echo ANDROID_HOME: %ANDROID_HOME%
) else if exist "%ANDROID_SDK_ROOT%" (
    echo ANDROID_SDK_ROOT: %ANDROID_SDK_ROOT%
) else (
    echo WARNING: No Android SDK environment variables found
)

echo.
echo Step 6: Build Tools Check
echo -------------------------
if exist "%ANDROID_HOME%\build-tools" (
    dir "%ANDROID_HOME%\build-tools"
) else if exist "%ANDROID_SDK_ROOT%\build-tools" (
    dir "%ANDROID_SDK_ROOT%\build-tools"
) else (
    echo Build tools directory not found
)

echo.
echo Step 7: CMake Check
echo ------------------
cmake --version 2>NUL
if %ERRORLEVEL% NEQ 0 (
    echo CMake not found in PATH
) else (
    echo CMake found and working
)

echo.
echo ===============================================
echo Diagnostic complete
echo ===============================================
pause
