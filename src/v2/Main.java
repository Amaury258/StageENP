/*
 * USB Analysor
 * Version 2
 * Par: Amaury DEMARQUE
 *      Stagiaire à l'École Nationale de Police de Montbéliard
 */

package v2;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import java.nio.channels.ClosedChannelException;
import javax.swing.filechooser.FileSystemView;

public class Main {
    public static void main(String[] args) throws Exception {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] drives = File.listRoots();
        HashMap<String,File> usbDrives = new HashMap<String,File>();
        if (drives != null && drives.length > 0) {
            for (File aDrive : drives) {
                if(fsv.getSystemTypeDescription(aDrive).contains("USB")){
                    //analyse antivirus + autorisation
                    System.out.println("Clé USB détectée : " + aDrive);
                    ejectUSB(aDrive.toPath());
                }
                /*if(isRemovableDrive(aDrive.toPath())){
                    //analyse antivirus + autorisation
                    System.out.println("Clé USB détectée : " + aDrive);
                    //ejectUSB(aDrive);
                }*/
            }
        }

        /*FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] roots = File.listRoots();
        
        for (File root : roots) {
            String type = fileSystemView.getSystemTypeDescription(root);
            boolean isRemovable = fileSystemView.isDrive(root) && fileSystemView.isFloppyDrive(root) && !fileSystemView.isFileSystemRoot(root) && fileSystemView.getSystemTypeDescription(root).contains("Removable");
            
            if (isRemovable) {
                // Le disque est amovible (clé USB)
                System.out.println("Clé USB détectée : " + root);
                
                // Éjecte la clé USB
                ejectUSB(root);
            } else {
                // Le disque n'est pas amovible (disque dur interne)
                System.out.println("Disque dur détecté : " + root);
            }
        }*/

        /*if(usbDrives.isEmpty()){
            System.out.println("Aucun périphérique USB détecté");
        } else {
            for (Map.Entry<String, File> entry : usbDrives.entrySet()) {
                System.out.println("Périphérique USB détecté : " + entry.getKey());
                //analyse antivirus + autorisation
                ejectUSB(entry.getKey());
            }
        }*/
    }

    private static boolean isRemovableDrive(Path rootDirectory) {
        File file = rootDirectory.toFile();
        return file.canRead() && file.canWrite() && file.isDirectory() && file.listFiles() != null && file.listFiles().length > 0;
    }

    private static void ejectUSB(Path path) throws Exception{
        String command = "devcon remove \"" + path.toString() + "\"";
        Process process = Runtime.getRuntime().exec(command);
        Scanner scanner = new Scanner(process.getInputStream());

        while (scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }

        process.waitFor();
        System.out.println("Clé USB " + path.toString() + " éjectée.");
    }
}
