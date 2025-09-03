@echo off
echo ============================================
echo FIXING CreateProcess Error - Java PATH Issue
echo ============================================

echo Step 1: Setting JAVA_HOME environment variable...
setx JAVA_HOME "C:\Program Files\Java\jdk-24"
echo JAVA_HOME set to: C:\Program Files\Java\jdk-24

echo.
echo Step 2: Adding Java to system PATH...
setx PATH "%PATH%;C:\Program Files\Java\jdk-24\bin"
echo Added C:\Program Files\Java\jdk-24\bin to PATH

echo.
echo Step 3: Setting environment variables for current session...
set JAVA_HOME=C:\Program Files\Java\jdk-24
set PATH=%PATH%;C:\Program Files\Java\jdk-24\bin

echo.
echo Step 4: Testing Java installation...
"C:\Program Files\Java\jdk-24\bin\java.exe" -version
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Java is working correctly
) else (
    echo ERROR: Java test failed
    goto :error
)

echo.
echo Step 5: Testing Gradle wrapper...
gradlew --version
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Gradle wrapper is now working!
    echo.
    echo You can now run:
    echo gradlew clean
    echo gradlew build
) else (
    echo Gradle test failed - trying direct approach...
    echo.
    echo Setting JAVA_HOME in current session and retrying...
    set JAVA_HOME=C:\Program Files\Java\jdk-24
    gradlew --version
    if %ERRORLEVEL% EQU 0 (
        echo SUCCESS: Gradle working with direct JAVA_HOME
    ) else (
        echo Still having issues - manual restart may be needed
    )
)

echo.
echo ============================================
echo SOLUTION COMPLETE
echo ============================================
echo.
echo IMPORTANT: If Gradle still doesn't work:
echo 1. Close this command prompt
echo 2. Open a NEW command prompt 
echo 3. Navigate back to your project directory
echo 4. Try: gradlew --version
echo.
echo Environment variables take effect in new command prompt sessions.
echo.

goto :end

:error
echo.
echo ERROR: Direct Java execution failed
echo This may indicate a deeper system issue.
echo.
echo Manual verification:
echo 1. Check if file exists: "C:\Program Files\Java\jdk-24\bin\java.exe"
echo 2. Try running it directly from Windows Explorer
echo.

:end
pause
