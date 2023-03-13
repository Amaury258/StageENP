package v2;

//import de librairie externe
import org.apache.commons.io.FileUtils;

//import de librairies standard de java
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Main2 {
    public static void main(String[] args) throws Exception {
        int i = 0;
        ArrayList<File> usb_connected = new ArrayList<>();
        while(true){
            if (i == 0) {
                System.out.println("Recherche de clé USB");
                i++;
            }

            Scanner sc = new Scanner(System.in);
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File[] newRoots = File.listRoots();
            Iterator<File> iter = usb_connected.iterator();
            while (iter.hasNext()) {
                File f = iter.next();
                if (!contains(newRoots,f)) {
                    iter.remove(); // supprimer l'élément en utilisant l'itérateur
                }
            }

            //parcours des disques
            for (File root : newRoots) {
                if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB") && !usb_connected.contains(root)) {
                    // la clé USB a été trouvée, faites quelque chose ici
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());

                    //Creation du tag
                    File tag = USBDetector.makeTAG(root);

                    //Recuperation du tag crypté
                    File crypted_tag = new File(root.getAbsolutePath() + "System Volume Information\\TAG");

                    if (!crypted_tag.exists()) {
                        System.out.println("La clé ne possede pas de fichier TAG, elle n'a donc pas été passé sur la machine blanche.");
                        USBDetector.eject(root);
                    } else {
                        boolean diff = false;
                        try {
                            File tmp = File.createTempFile("uncrypted", "", new File(System.getProperty("user.dir") + "\\tmp"));
                            File decrypted_tag = HashUtils.decrypt(crypted_tag, tmp, "StageInformatiqu".getBytes());

                            Scanner normalscan = new Scanner(tag);
                            Scanner cryptedscan = new Scanner(decrypted_tag);
                            Scanner scanvirus = new Scanner(decrypted_tag);
                            String linevirus = scanvirus.nextLine();
                            String line1,line2;
                            if(linevirus.substring(linevirus.indexOf(':')+1).trim().equals("true")){
                                System.out.println("Virus détecté !");
                                diff = true;
                            }
                            normalscan.nextLine();
                            cryptedscan.nextLine();
                            while ((normalscan.hasNextLine() && cryptedscan.hasNextLine()) && !diff) {
                                line1 = normalscan.nextLine();
                                line2 = cryptedscan.nextLine();
                                if (!line1.equals(line2)) {
                                    System.out.println("Le fichier TAG déchiffré est différent du fichier TAG créer depuis le programme!");
                                    diff = true;
                                }
                            }

                            scanvirus.close();
                            cryptedscan.close();
                            normalscan.close();

                        } catch (Exception e) {
                            System.out.println("Le fichier TAG a été modifié manuellement");
                            diff = true;
                        }

                        System.out.println("Appuyez sur \"Entré\" pour éjecter la clé usb");
                        sc.nextLine();

                        FileUtils.cleanDirectory(new File(System.getProperty("user.dir") + "\\tmp"));

                        if (diff) {
                            USBDetector.eject(root);
                        } else {
                            System.out.println("Aucune différence détecté, bonne continuation ^^");
                            usb_connected.add(root);
                        }
                    }
                    i--;
                }
            }
        }
    }

    private static boolean contains(File[] roots, File root) {
        for (File file : roots) {
            if (file.equals(root)) {
                return true;
            }
        }
        return false;
    }
}
