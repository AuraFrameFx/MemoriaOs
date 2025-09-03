@echo off
echo Resolving locked secure-comm\build directory...

REM Stop all processes that might be locking the directory
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im javaw.exe >nul 2>&1  
gradlew --stop >nul 2>&1

REM Use multiple deletion strategies
echo Attempting deletion strategies...

REM Strategy 1: PowerShell force delete
powershell -Command "if (Test-Path 'secure-comm\build') { Remove-Item -Recurse -Force 'secure-comm\build' -ErrorAction SilentlyContinue }"

REM Strategy 2: Take ownership and delete
takeown /f "secure-comm\build" /r /d y >nul 2>&1
icacls "secure-comm\build" /grant administrators:F /t >nul 2>&1
rmdir /s /q "secure-comm\build" 2>nul

REM Strategy 3: Force delete files first, then directory
del /f /s /q "secure-comm\build\*.*" 2>nul
rmdir /s /q "secure-comm\build" 2>nul

if exist "secure-comm\build" (
    echo WARNING: Directory still exists - trying final strategy
    rd /s /q "secure-comm\build" 2>nul
    del "secure-comm\build" /f /q 2>nul
) else (
    echo SUCCESS: secure-comm\build deleted
)

gradlew clean
