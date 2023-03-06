package v2;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Scanner;


public class Main2 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        File[] roots = File.listRoots();
        int i = 0;
        while (true) {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            if(i==0){
                System.out.println("Recherche de clé USB");
                i++;
            }
            File[] newRoots = File.listRoots();
            for (File root : newRoots) {
                if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB")) {
                    // la clé USB a été trouvée, faites quelque chose ici
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());
                    System.out.println("Appuyez sur \"Entrée\" pour éjecter la clé USB.");
                    try {
                        /*
                            ->IndexerVolumeGuid
                            ->lastModified()
                            ->HashCode
                         */

                        //int index = analyseIndexer()
                        int lastModif = analyseAll(root);
                        //int hash = analyseHashCode();

                        sc.nextLine();
                        eject(root);
                        i--;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            roots = newRoots;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int analyseAll(File usbfile){
        int res = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        for(File file : usbfile.listFiles()){
            if(file.isDirectory()){
                res = analyseAll(file);
                if(res == 1){
                    return res;
                }
            } else {
                //on prend depuis la bdd la date de la derniere modification
                String modified_at = null;
                if(sdf.format(file.lastModified()).equals(modified_at)){
                    res = 0;
                } else {
                    res = 1;
                }
            }
        }
        return res;
    }

    private static void eject(File drive) throws IOException {
        // Récupérer la lettre de lecteur de la clé USB
        String driveLetter = drive.getAbsolutePath().substring(0,1);

        String batchFilePath = System.getProperty("user.dir") + "\\src\\v2\\ejecter.cmd";
        Process process = Runtime.getRuntime().exec(" "+batchFilePath+" "+driveLetter+" "+System.getProperty("user.dir"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        System.out.println(output);
    }
}