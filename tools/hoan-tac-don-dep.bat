@echo off
setlocal
cd /d "%~dp0"
title Bat lai cac app da tat
call "%~dp0adb-ketnoi.bat"
if errorlevel 1 goto fail
echo Dang bat lai tat ca app da tat boi don-dep.bat...
for %%P in (com.amazon.amazonvideo.livingroom com.google.android.youtube.tvmusic com.google.android.videos com.google.android.play.games com.google.android.tvrecommendations com.google.android.backdrop com.google.android.marvin.talkback com.google.android.feedback com.android.tv.feedbackconsent com.android.printspooler com.android.wallpaper.livepicker com.apps.esticker com.seraphic.hbbtv.app com.frogmind.badland com.netflix.ninja com.google.android.katniss com.creative.fastscreen.tv com.mk.tv.meeting.service com.apps.logapp) do "%ADB%" -s %SER% shell pm enable %%P
echo Xong.
goto end
:fail
echo CO LOI. Chup man hinh cua so nay gui lai de duoc ho tro.
:end
pause
