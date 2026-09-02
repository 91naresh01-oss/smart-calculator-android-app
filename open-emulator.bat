@echo off
setlocal

set "SDK_ROOT=C:\Users\91nar\AppData\Local\Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\Android\Sdk"
if not exist "%SDK_ROOT%\emulator\emulator.exe" (
  set "SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
)

set "EMULATOR=%SDK_ROOT%\emulator\emulator.exe"
set "ADB=%SDK_ROOT%\platform-tools\adb.exe"
set "AVD_NAME=JungleGuardian_QA"

if not exist "%EMULATOR%" (
  echo Android Emulator was not found at:
  echo %EMULATOR%
  echo Please make sure Android SDK emulator is installed.
  pause
  exit /b 1
)

echo Checking available emulators...
"%EMULATOR%" -list-avds | findstr /X /C:"%AVD_NAME%" >nul
if errorlevel 1 (
  for /f "delims=" %%i in ('"%EMULATOR%" -list-avds') do (
    set "AVD_NAME=%%i"
    goto :found_avd
  )
  echo No Android Virtual Device found.
  pause
  exit /b 1
)

:found_avd
echo Launching emulator %AVD_NAME%...
start "" "%EMULATOR%" -avd "%AVD_NAME%" -gpu host
echo Emulator is starting. Please wait for the system to boot up.

