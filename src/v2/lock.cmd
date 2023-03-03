@echo off
setlocal

set "folderPath=%1"
set "userPrograms=NT AUTHORITY\INTERACTIVE:(OI)(CI)RX"

icacls "%folderPath%" /deny *S-1-1-0:(OI)(CI)F
icacls "%folderPath%" /grant "%userPrograms%" /inheritance:r

echo Le dossier %folderPath% a été verrouillé et les programmes ont accès en lecture et exécution uniquement.

pause
