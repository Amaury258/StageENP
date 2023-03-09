package v2;

//import de librairie externe
import org.apache.commons.io.FileUtils;

//import de librairies standard
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
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
            for(File connected : usb_connected){
                //Vérifier que l'usb connecté est toujours connecté : sinon le retirer de la liste
            }
            //parcours des disques
            for (File root : newRoots) {
                if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB") && !usb_connected.contains(root)){
                    // la clé USB a été trouvée, faites quelque chose ici
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());

                    //Creation du tag
                    File tag = USBDetector.makeTAG(root);

                    //Recuperation du tag crypté
                    File crypted_tag = new File(root.getAbsolutePath()+"System Volume Information\\TAG");

                    if(!crypted_tag.exists()){
                        System.out.println("La clé ne possede pas de fichier TAG, elle n'a donc pas été passé sur la machine blanche.");
                        USBDetector.eject(root);
                    } else {
                        File tmp = File.createTempFile("uncrypted","",new File(System.getProperty("user.dir")+"\\tmp"));
                        File decrypted_tag = HashUtils.decrypt(crypted_tag, tmp, "StageInformatiqu".getBytes());

                        Scanner normalScan = new Scanner(tag);
                        Scanner cryptedScan = new Scanner(decrypted_tag);
                        String line1 ="";
                        String line2 ="";
                        boolean diff = false;
                        while(normalScan.hasNextLine() && cryptedScan.hasNextLine()){
                            line1 = normalScan.nextLine();
                            line2 = cryptedScan.nextLine();
                            diff = !line1.equals(line2);
                            if(diff){
                                break;
                            }
                        }

                        FileUtils.cleanDirectory(new File(System.getProperty("user.dir")+"\\tmp"));

                        sc.nextLine();

                        if(diff){
                            System.out.println("Différence détecté : \n");
                            System.out.println("\tmakeTAG() : \n\t"+
                                    line1+"\n");
                            System.out.println("\tTAG chiffré : \n\t"+
                                    line2+"\n");
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
}
