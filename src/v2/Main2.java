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
        //Liste des clé usb connecté au pc
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
            //Vérifier si les clés usb de la liste sont toujours connecté
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

                            Scanner scankey = new Scanner(new File(root.getAbsolutePath()+"System Volume Information\\IndexerVolumeGuid"));
                            String guid = scankey.nextLine();
                            scankey.close();

                            String hashkey = guid+root.getName();
                            String hashkey = HashUtils.hashString(hashkey,"SHA-256");

                            File decrypted_tag = HashUtils.decrypt(crypted_tag, tmp, hashkey);

                            //Lecture du fichier TAG (déchiffré)
                            Scanner normalscan = new Scanner(tag);
                            Scanner cryptedscan = new Scanner(decrypted_tag);
                            Scanner scanvirus = new Scanner(decrypted_tag);
                            String linevirus = scanvirus.nextLine();
                            String line1,line2;
                            //Verification de l'attribut virus, si il est à 'true', on ejecte
                            if(linevirus.substring(linevirus.indexOf(':')+1).trim().equals("true")){
                                System.out.println("Virus détecté !");
                                diff = true;
                                USBDetector.eject(root);
                            }

                            //Lecture de tout le fichier TAG (déchiffré et par la methode makeTAG)
                            normalscan.nextLine();
                            cryptedscan.nextLine();
                            while ((normalscan.hasNextLine() && cryptedscan.hasNextLine()) && !diff) {
                                line1 = normalscan.nextLine();
                                line2 = cryptedscan.nextLine();
                                //Si les lignes sont différentes, on ejecte
                                if (!line1.equals(line2)) {
                                    System.out.println("Le fichier TAG déchiffré est différent du fichier TAG créer depuis le programme!");
                                    diff = true;
                                    USBDetector.eject(root);
                                }
                            }

                            //Fermeture des flux de lecture de fichier
                            scanvirus.close();
                            cryptedscan.close();
                            normalscan.close();

                        } catch (Exception e) {
                            //Si le fichier TAG (chiffré) a été modifié, on ejecte
                            System.out.println("Le fichier TAG a été modifié manuellement");
                            diff = true;
                            USBDetector.eject(root);
                        }

                        //Suppression des fichiers temporaires
                        FileUtils.cleanDirectory(new File(System.getProperty("user.dir") + "\\tmp"));

                        //Si tout ce passe bien, on peut utiliser la clé
                        if (!diff) {
                            System.out.println("Aucune différence détecté, bonne continuation ^^");
                            usb_connected.add(root);
                        }
                    }
                    i--;
                }
            }
        }
    }

    /**
     * Methode pour vérifier si un objet File est dans un tavbleau de File
     *
     * @param roots
     * @param root
     * @return
     */
    private static boolean contains(File[] roots, File root) {
        for (File file : roots) {
            if (file.equals(root)) {
                return true;
            }
        }
        return false;
    }
}
