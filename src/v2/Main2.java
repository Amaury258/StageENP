package v2;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main2 {
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

                    //Recuperation du tag crypté
                    File crypted_tag = new File(root.getAbsolutePath()+"System Volume Information\\TAG");

                    File tmp = File.createTempFile("uncrypted","",new File(System.getProperty("user.dir")));
                    tmp.deleteOnExit();
                    File decrypted_tag = HashUtils.decrypt(crypted_tag, tmp, "StageInformatiqu".getBytes());

                    FileReader normalFileReader = new FileReader(tag);
                    FileReader cryptedFileReader = new FileReader(decrypted_tag);

                    BufferedReader normalReader = new BufferedReader(normalFileReader);
                    BufferedReader cryptedReader = new BufferedReader(cryptedFileReader);

                    /*String line,line2;
                    boolean diff = false;
                    while((((line=normalReader.readLine())!=null) && (line2=cryptedReader.readLine())!=null)) {
                        if (!line.equals(line2)) {
                            System.out.println("Différence détecté : \n" +
                                    line + "\n" +
                                    line2 + "\n");
                            diff = true;
                            break;
                        }
                    }*/

                    String line1 = normalReader.readLine();
                    String line2 = cryptedReader.readLine();
                    System.out.println("Tag de la méthode makeTAG() : \n");
                    while(line1 != null){
                        System.out.println(line1);
                        line1 = normalReader.readLine();
                    }

                    System.out.println("\nTag décrypté : \n");
                    while(line2 != null){
                        System.out.println(line2);
                        line2 = cryptedReader.readLine();
                    }

                    sc.nextLine();

                    //if(diff){
                        USBDetector.eject(root);
                    //}

                    tag.delete();

                    i--;
                }
            }

        }
    }
}
