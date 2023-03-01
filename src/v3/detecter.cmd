@echo off
setlocal EnableDelayedExpansion

set "program=MyProgram.jar"

echo "Attente de la connexion d'une clé USB..."
:loop
for /f "tokens=1,2" %%a in ('wmic logicaldisk where "drivetype=2 and mediatype=12" get DeviceID^, VolumeName ^| find ":"') do (
  set "drive=%%a"
  set "label=%%b"
  echo "Clé USB connectée : %drive% (%label%)"
  start "" java -jar "%program%" "%drive%"
  goto :endloop
)

echo "Aucune clé USB détectée. Attente de connexion..."
timeout /t 1 >nul
goto :loop

:endloop
echo "Attente de la déconnexion de la clé USB..."
:loop2
for /f "tokens=1,2" %%a in ('wmic logicaldisk where "drivetype=2 and mediatype=12" get DeviceID^, VolumeName ^| find ":"') do (
  set "drive=%%a"
  set "label=%%b"
  timeout /t 1 >nul
  for /f "tokens=1,2" %%c in ('wmic logicaldisk where "drivetype=2 and mediatype=12" get DeviceID^, VolumeName ^| find ":"') do (
    if not "%%c"=="%drive%" (
      echo "Clé USB déconnectée : %drive% (%label%)"
      goto :endloop2
    )
  )
)

echo "Aucune clé USB détectée. Attente de déconnexion..."
timeout /t 1 >nul
goto :loop2

:endloop2
echo "Fin du programme."
