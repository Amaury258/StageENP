/*
 * USB Analysor
 * Version 2
 * Par: Amaury DEMARQUE
 * Stagiaire à l'École Nationale de Police de Montbéliard
 */

package v2;

//import de librairie standard
import javax.swing.filechooser.FileSystemView;
import java.io.*;
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
                    // la clé USB a été trouvée, faites quelque chose ici
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());

                    //Creation du tag
                    File tag = USBDetector.makeTAG(root);
                    FileReader fileReader = new FileReader(tag);
                    BufferedReader reader = new BufferedReader(fileReader);

                    //Lecture du tag pour vérifier qu'il y a bien ce qu'on veux
                    String line = reader.readLine();
                    while(line != null){
                        System.out.println(line);
                        line = reader.readLine();
                    }

                    reader.close();
                    fileReader.close();


                    //Chiffrement du tag + sauvegarde sur la clé usb
                    File CRYPTED_TAG = HashUtils.encrypt(tag, root.getAbsolutePath()+"System Volume Information\\TAG",
                            "StageInformatiqu".getBytes());



                    System.out.println("\nFichier crypté\n");

                    tag.delete();

                    //Lecture du fichier chiffré pour s'assurer qu'il est chiffré
                    fileReader = new FileReader(CRYPTED_TAG);
                    reader = new BufferedReader(fileReader);
                    line = reader.readLine();
                    while(line != null){
                        System.out.println(line);
                        line = reader.readLine();
                    }

                    reader.close();
                    fileReader.close();

                    sc.nextLine();

                    //Ejection de la clé usb + affichage du résultat
                    USBDetector.eject(root);
                    i--;
                }
            }

        }
    }
}
