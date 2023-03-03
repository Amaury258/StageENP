@echo off
setlocal

set "folderPath=%1%"

icacls "%folderPath%" /reset

echo Le dossier %folderPath% a été déverrouillé.

pause
