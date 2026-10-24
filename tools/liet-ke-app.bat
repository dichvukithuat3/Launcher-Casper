@echo off
setlocal
cd /d "%~dp0"
title Liet ke ung dung tren TV
call "%~dp0adb-ketnoi.bat"
if errorlevel 1 goto fail

set "OUT=%~dp0danh-sach-app.txt"
> "%OUT%" echo === RAM ===
"%ADB%" -s %SER% shell head -3 /proc/meminfo >> "%OUT%"
>> "%OUT%" echo.
>> "%OUT%" echo === BO NHO ===
"%ADB%" -s %SER% shell df -h /data >> "%OUT%"
>> "%OUT%" echo.
>> "%OUT%" echo === APP CAI THEM ===
"%ADB%" -s %SER% shell pm list packages -3 >> "%OUT%"
>> "%OUT%" echo.
>> "%OUT%" echo === APP HE THONG ===
"%ADB%" -s %SER% shell pm list packages -s >> "%OUT%"
>> "%OUT%" echo.
>> "%OUT%" echo === APP DA TAT ===
"%ADB%" -s %SER% shell pm list packages -d >> "%OUT%"
echo.
echo Da luu vao danh-sach-app.txt. Gui file nay cho Claude de chon app rac can tat.
start notepad "%OUT%"
goto end

:fail
echo CO LOI. Chup man hinh cua so nay gui lai de duoc ho tro.

:end
pause
