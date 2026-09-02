@echo off
setlocal

set "SDK_ROOT=C:\Users\91nar\AppData\Local\Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\Android\Sdk"
if not exist "%SDK_ROOT%\emulator\emulator.exe" (
  set "SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
)

set "EMULATOR=%SDK_ROOT%\emulator\emulator.exe"
set "ADB=%SDK_ROOT%\platform-tools\adb.exe"
set "AVD_NAME=JungleGuardian_QA"

echo ===================================================
echo       SMART CALCULATOR - EMULATOR RUNNER
echo ===================================================
echo.

echo [1/3] Checking emulator...
"%ADB%" devices | findstr "emulator-" >nul
if errorlevel 1 (
    echo Starting emulator %AVD_NAME%...
    start "" "%EMULATOR%" -avd "%AVD_NAME%" -gpu host
    echo Waiting for emulator device to be ready...
    "%ADB%" wait-for-device
    "%ADB%" shell "while [[ \"$(getprop sys.boot_completed)\" != \"1\" ]]; do sleep 1; done"
) else (
    echo Emulator is already running!
)

echo.
echo [2/3] Installing latest APK...
"%ADB%" install -r "app\build\outputs\apk\debug\app-debug.apk"

echo.
echo [3/3] Opening Smart Calculator on screen...
"%ADB%" shell am start -n com.naresh.smartcalculatornote/.MainActivity

echo.
echo ===================================================
echo [SUCCESS] App is now open on your emulator screen!
echo ===================================================
timeout /t 5
