@echo off
setlocal
cd /d "%~dp0"
title Khoi phuc giao dien goc
call "%~dp0adb-ketnoi.bat"
if errorlevel 1 goto fail

echo.
echo Dang bat lai launcher goc...
for %%P in (com.google.android.tvlauncher com.google.android.apps.tv.launcherx com.google.android.leanbacklauncher) do "%ADB%" -s %SER% shell pm enable %%P
"%ADB%" -s %SER% shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity >nul 2>&1
set "UN="
set /p UN=Go Y de go luon Kinh Launcher (Enter = giu lai): 
if /i "%UN%"=="Y" "%ADB%" -s %SER% uninstall vn.liteglass.launcher
"%ADB%" -s %SER% shell input keyevent KEYCODE_HOME
echo.
echo Da khoi phuc. Bam Home tren remote; neu TV hoi chon man hinh chinh, chon giao dien goc.
goto end

:fail
echo CO LOI. Chup man hinh cua so nay gui lai de duoc ho tro.

:end
pause
