@echo off
echo ================================================
echo GenesisOS Force Clean - Locked Directory Resolver
echo ================================================

echo Step 1: Stopping all Gradle processes...
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im javaw.exe >nul 2>&1
gradlew --stop >nul 2>&1

echo Step 2: Stopping Android Studio/IntelliJ processes...
taskkill /f /im studio64.exe >nul 2>&1
taskkill /f /im idea64.exe >nul 2>&1

echo Step 3: Force deleting locked build directories...

REM Use PowerShell to force delete locked directories
powershell -Command "if (Test-Path 'secure-comm\build') { Remove-Item -Recurse -Force 'secure-comm\build' -ErrorAction SilentlyContinue }"
powershell -Command "if (Test-Path 'collab-canvas\build') { Remove-Item -Recurse -Force 'collab-canvas\build' -ErrorAction SilentlyContinue }"
powershell -Command "if (Test-Path 'oracle-drive-integration\build') { Remove-Item -Recurse -Force 'oracle-drive-integration\build' -ErrorAction SilentlyContinue }"

REM Force delete using rmdir with specific flags
rmdir /s /q "secure-comm\build" 2>nul
rmdir /s /q "collab-canvas\build" 2>nul
rmdir /s /q "oracle-drive-integration\build" 2>nul

echo Step 4: Clearing all module build directories...
for /d %%G in (module-*) do (
    if exist "%%G\build" (
        echo Cleaning %%G...
        rmdir /s /q "%%G\build" 2>nul
    )
)

echo Step 5: Clearing core module build directories...
rmdir /s /q "core-module\build" 2>nul
rmdir /s /q "feature-module\build" 2>nul
rmdir /s /q "datavein-oracle-native\build" 2>nul
rmdir /s /q "colorblendr\build" 2>nul
rmdir /s /q "romtools\build" 2>nul
rmdir /s /q "sandbox-ui\build" 2>nul

echo Step 6: Clearing app build directory...
rmdir /s /q "app\build" 2>nul

echo Step 7: Clearing root build directory...
rmdir /s /q "build" 2>nul

echo Step 8: Clearing Gradle caches...
rmdir /s /q ".gradle" 2>nul

echo Step 9: Clearing native build artifacts...
for /d /r . %%G in (.cxx) do (
    if exist "%%G" (
        echo Removing native build cache: %%G
        rmdir /s /q "%%G" 2>nul
    )
)

echo Step 10: Alternative force delete using takeown (Windows Admin)
takeown /f "secure-comm\build" /r >nul 2>&1
icacls "secure-comm\build" /grant administrators:F /t >nul 2>&1
rmdir /s /q "secure-comm\build" 2>nul

echo.
echo ============================================
echo NUCLEAR OPTION: Using DEL command
echo ============================================
del /f /s /q "secure-comm\build\*.*" 2>nul
del /f /s /q "collab-canvas\build\*.*" 2>nul
del /f /s /q "oracle-drive-integration\build\*.*" 2>nul

echo.
echo Verification: Checking if locked directories still exist...
if exist "secure-comm\build" (
    echo WARNING: secure-comm\build still exists - may require manual deletion
) else (
    echo ✅ secure-comm\build successfully deleted
)

if exist "collab-canvas\build" (
    echo WARNING: collab-canvas\build still exists
) else (
    echo ✅ collab-canvas\build successfully deleted
)

if exist "oracle-drive-integration\build" (
    echo WARNING: oracle-drive-integration\build still exists
) else (
    echo ✅ oracle-drive-integration\build successfully deleted
)

echo.
echo ===============================
echo FINAL STEP: Testing Gradle Clean
echo ===============================
echo Running gradlew clean...
gradlew clean

echo.
echo ================================================
echo Force clean operation completed!
echo ================================================
pause
