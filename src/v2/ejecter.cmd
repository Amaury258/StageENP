@echo off
set drive=%1
set eject="%2\lib\USB Disk Ejector\USB_Disk_Eject.exe"
if exist %eject% (
    start "" %eject% /REMOVELETTER %drive%
    echo "La clé USB %drive% à été éjecté avec succés."
) else (
    echo "Impossible de trouver l'utilitaire USB Disk Ejector."
)
