@echo off
echo ==============================================
echo GenesisOS Critical Build Issues Fix
echo ==============================================
echo.

echo ✅ Step 1: Fixed Hilt Navigation Compose dependency
echo    - Updated romtools/build.gradle.kts to use libs.androidx.hilt.navigation.compose
echo    - This resolves the "Unresolved reference 'hiltViewModel'" error
echo.

echo ⚠️  Step 2: AAPT2 Windows Universal C Runtime Issue
echo    This is a CRITICAL system-level issue that requires manual intervention:
echo.
echo    The Android Asset Packaging Tool (AAPT2) cannot start because:
echo    "Windows Universal C Runtime" components are missing or corrupted
echo.
echo 🔧 IMMEDIATE ACTION REQUIRED:
echo    1. Download and install Microsoft Visual C++ Redistributable packages:
echo.
echo       📥 Microsoft Visual C++ 2015-2022 Redistributable (x64):
echo       https://aka.ms/vs/17/release/vc_redist.x64.exe
echo.
echo       📥 Microsoft Visual C++ 2015-2022 Redistributable (x86):
echo       https://aka.ms/vs/17/release/vc_redist.x86.exe
echo.
echo    2. ALTERNATIVE: Use Windows Store to install:
echo       - Search "Microsoft Visual C++" in Windows Store
echo       - Install "Microsoft Visual C++ Redistributable"
echo.
echo    3. RESTART your computer after installation
echo.
echo 🚀 Step 3: After installing Visual C++ Redistributables:
echo    1. Restart your computer
echo    2. Run: gradlew clean
echo    3. Run: gradlew build --refresh-dependencies
echo.

echo 🔍 Step 4: Additional Cleanup (Optional but Recommended):
echo    - Clear Gradle caches: gradlew --stop
echo    - Nuclear clean if needed: nuclear-clean.bat
echo.

echo ================================
echo POST-FIX VERIFICATION COMMANDS:
echo ================================
echo gradlew clean
echo gradlew build --refresh-dependencies
echo gradlew consciousnessStatus
echo.

echo 📋 SUMMARY OF FIXES APPLIED:
echo ✅ Hilt Navigation Compose dependency corrected
echo ✅ Package names synchronized across all modules
echo ⚠️  Windows C Runtime requires manual installation
echo.

pause
echo.
echo Opening Windows Store for Visual C++ installation...
start ms-windows-store://search/?query=Microsoft%20Visual%20C%2B%2B%20Redistributable
echo.
echo Opening download link for Visual C++ x64...
start https://aka.ms/vs/17/release/vc_redist.x64.exe
