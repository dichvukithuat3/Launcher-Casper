@echo off
setlocal
cd /d "%~dp0"
title Cai dat Kinh Launcher
if not exist "%~dp0KinhLauncher.apk" goto noapk
call "%~dp0adb-ketnoi.bat"
if errorlevel 1 goto fail

echo.
echo [3/5] Dang cai Kinh Launcher len TV...
"%ADB%" -s %SER% install -r "%~dp0KinhLauncher.apk"
if errorlevel 1 goto fail

echo [4/5] Dat lam man hinh chinh va mo thu...
"%ADB%" -s %SER% shell cmd package set-home-activity vn.liteglass.launcher/.MainActivity
"%ADB%" -s %SER% shell am start -n vn.liteglass.launcher/.MainActivity
echo.
echo Hay nhin len TV: giao dien moi da hien ra va dieu khien bang remote duoc chua?
set "OK="
set /p OK=Go Y roi Enter de tat giao dien cu (Enter = de sau): 
if /i not "%OK%"=="Y" goto keep

echo [5/5] Tat launcher goc (bat lai bang khoi-phuc.bat)...
for %%P in (com.google.android.tvlauncher com.google.android.apps.tv.launcherx com.google.android.leanbacklauncher) do "%ADB%" -s %SER% shell pm disable-user --user 0 %%P
"%ADB%" -s %SER% shell input keyevent KEYCODE_HOME
echo.
echo XONG! Bam nut Home tren remote de kiem tra.
goto end

:keep
echo.
echo Da cai xong, giao dien goc van giu nguyen.
echo Khi san sang, chay lai file nay va go Y.
goto end

:noapk
echo Khong thay file KinhLauncher.apk trong cung thu muc. Hay giai nen day du file zip.
goto end

:fail
echo.
echo CO LOI. Chup man hinh cua so nay gui lai de duoc ho tro.

:end
pause
