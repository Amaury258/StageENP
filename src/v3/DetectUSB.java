/*
 * USB Analysor
 * Version 3
 * Par: Amaury DEMARQUE
 *      Stagiaire à l'École Nationale de Police de Montbéliard
 */

package v3;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

public class DetectUSB {

    private Set<File> usbFileset;

    public DetectUSB(){
        try {
            usbFileset = detect();
        } catch (Exception e) {
            // TODO: handle exception
        }        
    }

    public Set detect() throws Exception {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] drives = File.listRoots();
        Set<File> sf = new HashSet<>();
        if (drives != null && drives.length > 0) {
            for (File aDrive : drives) {
                if(fsv.getSystemTypeDescription(aDrive).contains("USB")){
                    sf.add(aDrive);
                }
            }
        }
        return sf;
    }

    public void ejectUSB(File usbfile) throws IOException {
        
        // exécute la commande de démontage pour le lecteur
        Process p = Runtime.getRuntime().exec("mountvol " + usbfile.getPath() + " /D");
        
        // attends que le processus se termine
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("La clé USB a été éjectée avec succès.");
    }

    public Set<File> getUsbFileSet(){ return this.usbFileset; }
}