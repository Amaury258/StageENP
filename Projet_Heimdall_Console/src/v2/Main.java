/*
 * USB Analysor
 * Version 2
 * Par: Amaury DEMARQUE
 * Stagiaire à l'École Nationale de Police de Montbéliard
 */

package v2;

//import de librairie standard de java
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int i = 0;
        while(true){
            if (i == 0) {
                System.out.println("Recherche de clé USB");
                i++;
            }

            Scanner sc = new Scanner(System.in);
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File[] newRoots = File.listRoots();
            //parcours des disques
            for (File root : newRoots) {
                if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB")){
                    // la clé USB a été trouvée
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());

                    //Creation du tag
                    File tag = USBDetector.makeTAGAntivirus(root);

                    FileReader fileReader = new FileReader(tag);
                    BufferedReader reader = new BufferedReader(fileReader);

                    //Lecture du tag pour vérifier qu'il y a bien ce qu'on veut
                    String line = reader.readLine();
                    while(line != null){
                        System.out.println(line);
                        line = reader.readLine();
                    }

                    reader.close();
                    fileReader.close();

                    //prendre l'id de la clé, son nom et sa lettre de lecteur, hasher le tout
                    Scanner scanid = new Scanner(new File(root.getAbsolutePath()+"System Volume Information\\IndexerVolumeGuid"));
                    String hashkey = scanid.nextLine();
                    scanid.close();

                    hashkey = HashUtils.hashString(hashkey,"SHA-256");

                    System.out.println("Hashkey : "+hashkey);

                    //Chiffrement du tag + sauvegarde sur la clé usb
                    HashUtils.encrypt(tag, root.getAbsolutePath()+"System Volume Information\\TAG",
                            hashkey);

                    System.out.println("\nFichier crypté");

                    tag.delete();

                    System.out.println("Appuyez sur \"Entré\" pour éjecter la clé usb");
                    sc.nextLine();

                    //Ejection de la clé usb
                    USBDetector.eject(root);
                    i--;
                }
            }
        }
    }
}
