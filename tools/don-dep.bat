@echo off
setlocal
cd /d "%~dp0"
title Don dep app rac tren TV
call "%~dp0adb-ketnoi.bat"
if errorlevel 1 goto fail

echo.
echo === Nhom 1: app rac an toan de tat ===
echo Prime Video, YouTube Music, Google TV/Phim, Play Games, goi y noi dung,
echo hinh nen Ambient, TalkBack, phan hoi, in an, hinh nen dong, sticker trung bay,
echo HbbTV (chuan truyen hinh chau Au), game Badland.
set "X="
set /p X=Go Y de tat nhom 1 (Enter = bo qua): 
if /i not "%X%"=="Y" goto group2
for %%P in (com.amazon.amazonvideo.livingroom com.google.android.youtube.tvmusic com.google.android.videos com.google.android.play.games com.google.android.tvrecommendations com.google.android.backdrop com.google.android.marvin.talkback com.google.android.feedback com.android.tv.feedbackconsent com.android.printspooler com.android.wallpaper.livepicker com.apps.esticker com.seraphic.hbbtv.app com.frogmind.badland) do "%ADB%" -s %SER% shell pm disable-user --user 0 %%P

:group2
echo.
echo === Nhom 2: tuy nhu cau, hoi tung muc ===
set "X="
set /p X=Tat Netflix? Go Y de tat, Enter de giu: 
if /i not "%X%"=="Y" goto skip0
for %%P in (com.netflix.ninja) do "%ADB%" -s %SER% shell pm disable-user --user 0 %%P
:skip0
set "X="
set /p X=Tat Tro ly giong noi Google (se mat tim kiem bang giong noi tren remote)? Go Y de tat, Enter de giu: 
if /i not "%X%"=="Y" goto skip1
for %%P in (com.google.android.katniss) do "%ADB%" -s %SER% shell pm disable-user --user 0 %%P
:skip1
set "X="
set /p X=Tat App chieu man hinh FastScreen (Chromecast van giu nguyen)? Go Y de tat, Enter de giu: 
if /i not "%X%"=="Y" goto skip2
for %%P in (com.creative.fastscreen.tv) do "%ADB%" -s %SER% shell pm disable-user --user 0 %%P
:skip2
set "X="
set /p X=Tat Dich vu hop truc tuyen va ghi log cua hang? Go Y de tat, Enter de giu: 
if /i not "%X%"=="Y" goto skip3
for %%P in (com.mk.tv.meeting.service com.apps.logapp) do "%ADB%" -s %SER% shell pm disable-user --user 0 %%P
:skip3

echo.
echo Da xong. Moi app deu bat lai duoc bang hoan-tac-don-dep.bat
set "X="
set /p X=Go Y de khoi dong lai TV ngay cho nhe may (Enter = de sau): 
if /i "%X%"=="Y" "%ADB%" -s %SER% reboot
goto end

:fail
echo CO LOI. Chup man hinh cua so nay gui lai de duoc ho tro.

:end
pause
