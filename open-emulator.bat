@echo off
setlocal

set "SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
set "EMULATOR=%SDK_ROOT%\emulator\emulator.exe"
set "ADB=%SDK_ROOT%\platform-tools\adb.exe"
set "AVD_NAME=SmartCalculatorNewApi35"

if not exist "%EMULATOR%" (
  echo Android Emulator was not found at:
  echo %EMULATOR%
  echo Open Android Studio once and install Android Emulator from SDK Manager.
  pause
  exit /b 1
)

"%EMULATOR%" -list-avds | findstr /X /C:"%AVD_NAME%" >nul
if errorlevel 1 (
  echo The %AVD_NAME% emulator is not available.
  echo Open Android Studio ^> Device Manager and create an API 35 phone emulator.
  pause
  exit /b 1
)

echo Opening %AVD_NAME%...
start "Smart Calculator Emulator" "%EMULATOR%" -avd %AVD_NAME% -gpu host
echo Wait until Android home screen appears, then run or install the app.
pause
