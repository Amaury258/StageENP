@echo off
set "deviceName=\\?\Volume{08b19e2d-b69f-11ed-8367-e5b522bdb875}\"
set "driveLetter=D:"

echo Mounting USB drive...
mountvol %driveLetter% %deviceName%

echo USB drive mounted at %driveLetter%.
pause
