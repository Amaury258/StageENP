/*
 * USB Analysor
 * Version 2
 * Par: Amaury DEMARQUE
 *      Stagiaire à l'École Nationale de Police de Montbéliard
 */

package v2;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.filechooser.FileSystemView;

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

                    //Ejection de la clé usb
                    USBDetector.eject(root);
                    i--;
                }
            }

        }
    }
}
