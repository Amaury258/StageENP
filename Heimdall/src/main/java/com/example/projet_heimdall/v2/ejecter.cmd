@echo off
set drive=%1
set eject="USB Disk Ejector\USB_Disk_Eject.exe"
if exist %eject% (
    start "" %eject% /REMOVELETTER %drive%
    echo "Le périphérique USB %drive%:\ à été éjecté avec succés."
) else (
    echo "Impossible de trouver l'utilitaire USB Disk Ejector."
)
