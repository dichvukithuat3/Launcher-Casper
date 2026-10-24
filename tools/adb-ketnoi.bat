@echo off
rem Dung chung cho cac file .bat khac: tai ADB va ket noi TV.
rem Dat 2 bien: ADB (duong dan adb.exe) va SER (IP:PORT cua TV).
set "ADB=%~dp0platform-tools\adb.exe"
if exist "%ADB%" goto haveadb
echo [1/5] Dang tai cong cu ADB tu Google (chi lan dau, khoang 10MB)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/platform-tools-latest-windows.zip' -OutFile '%~dp0pt.zip'; Expand-Archive -Path '%~dp0pt.zip' -DestinationPath '%~dp0' -Force; Remove-Item '%~dp0pt.zip'"
if not exist "%ADB%" goto noadb
:haveadb
"%ADB%" start-server >nul 2>&1
echo [2/5] Dang ket noi TV...
echo      (Neu TV hien hop thoai "Cho phep go loi?", chon "Luon cho phep" roi OK)
set "SER="
if exist "%~dp0tv.txt" set /p SER=<"%~dp0tv.txt"
if not defined SER goto try5555
"%ADB%" connect %SER% 2>&1 | find /i "connected to" >nul
if not errorlevel 1 goto ok

:try5555
set "IP="
set /p IP=Nhap IP cua TV (Enter = 192.168.3.17): 
if "%IP%"=="" set "IP=192.168.3.17"
set "SER=%IP%:5555"
"%ADB%" connect %SER% 2>&1 | find /i "connected to" >nul
if not errorlevel 1 goto ok

echo.
echo Can ghep noi lan dau. Tren TV vao:
echo   Cai dat ^> Tuy chon nha phat trien ^> Go loi khong day ^> Ghep noi thiet bi bang ma
set "PAIR="
set /p PAIR=Nhap IP:PORT trong o ghep noi, vd 192.168.3.17:37123 : 
set "CODE="
set /p CODE=Nhap ma ghep noi 6 so: 
"%ADB%" pair %PAIR% %CODE%
echo.
echo Bay gio xem dong "Dia chi IP va cong" o man hinh Go loi khong day.
set "SER="
set /p SER=Nhap IP:PORT do, vd 192.168.3.17:41234 : 
"%ADB%" connect %SER% 2>&1 | find /i "connected to" >nul
if errorlevel 1 goto noconn

:ok
"%ADB%" -s %SER% wait-for-device
echo      Da ket noi TV: %SER%
> "%~dp0tv.txt" echo %SER%
exit /b 0

:noadb
echo Khong tai duoc ADB. Kiem tra mang Internet cua may tinh.
exit /b 1

:noconn
echo Khong ket noi duoc TV. Kiem tra TV va may tinh cung mang Wi-Fi, va da bat Go loi khong day.
exit /b 1
