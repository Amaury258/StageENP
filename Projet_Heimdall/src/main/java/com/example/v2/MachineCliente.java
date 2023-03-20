package com.example.v2;

//import de librairie externe
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

//import de librairies standard de java
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class MachineCliente extends Task {

    private Label lbl1, lbl2;
    private Stage stage;
    public MachineCliente(Stage stage, Label lbl, Label lbl2){
        this.stage = stage;
        this.lbl1 = lbl;
        this.lbl2 = lbl2;
    }
    public synchronized Void call() throws Exception {
        //Liste des clé usb connecté au pc
        ArrayList<File> usb_connected = new ArrayList<>();
        while(true){
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
                    AtomicBoolean diff = new AtomicBoolean(false);
                    Platform.runLater(() -> {
                        stage.setIconified(false);
                        stage.setAlwaysOnTop(true);
                        // la clé USB a été trouvée, faites quelque chose ici
                        lbl1.setText("Clé USB détecté : " + root.getAbsolutePath());
                    });
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    //Recuperation du tag crypté
                    File crypted_tag = new File(root.getAbsolutePath() + "System Volume Information\\TAG");

                    if (!crypted_tag.exists()) {
                        Platform.runLater(() -> {
                            lbl1.setText(lbl1.getText() + "\nLa clé ne possede pas de fichier TAG\nEjection de la clé");
                        });
                        diff.set(true);
                        try {
                            Thread.sleep(750);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        diff.set(true);
                    } else {

                        try {
                            Platform.runLater(() -> {
                                lbl1.setText(lbl1.getText() + "\nDéchiffrage du TAG");
                            });
                            try {
                                Thread.sleep(750);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            File tmp = File.createTempFile("uncrypted", "", new File(System.getProperty("user.dir") + "\\tmp"));


                            //prendre l'id de la clé, son nom et sa lettre de lecteur, hasher le tout
                            Scanner scanid = new Scanner(new File(root.getAbsolutePath() + "System Volume Information\\IndexerVolumeGuid"));
                            String hashkey = scanid.nextLine();

                            scanid.close();

                            hashkey = HashUtils.hashString(hashkey, "SHA-256");

                            File decrypted_tag = HashUtils.decrypt(crypted_tag, tmp, hashkey.getBytes());


                            //Le programme n'affiche plus juste apres cette ligne
                            Platform.runLater(() -> {
                                lbl1.setText(lbl1.getText() + "\nCréation du TAG temporaire");
                            });
                            try {
                                Thread.sleep(750);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            //Creation du tag temporaire
                            File tag = USBDetector.makeTAG(root);

                            //Lecture du fichier TAG (déchiffré)
                            Scanner normalscan = new Scanner(tag);
                            Scanner cryptedscan = new Scanner(decrypted_tag);
                            String line1, line2;
                            String linevirus = getAttribut(decrypted_tag,"Virus");

                            Platform.runLater(() -> {
                                lbl1.setText(lbl1.getText() + "\nComparaison des deux TAG");
                            });
                            try {
                                Thread.sleep(750);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Platform.requestNextPulse();
                            //Verification de l'attribut virus, si il est à 'true', on ejecte
                            if (!linevirus.isEmpty() && linevirus.equals("true")) {
                                Platform.runLater(() -> {
                                    lbl1.setText(lbl1.getText() + "\nVirus détecté\nEjection de la clé");
                                });
                                try {
                                    Thread.sleep(750);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                diff.set(true);
                            } else {
                                //Lecture de tout le fichier TAG (déchiffré et par la methode makeTAG)
                                normalscan.nextLine();
                                cryptedscan.nextLine();
                                while (normalscan.hasNextLine() && cryptedscan.hasNextLine()) {
                                    line1 = normalscan.nextLine();
                                    line2 = cryptedscan.nextLine();
                                    //Si les lignes sont différentes, on ejecte
                                    if (!line1.equals(line2)) {
                                        Platform.runLater(() -> {
                                            lbl1.setText(lbl1.getText() + "\nLe fichier TAG de la clé est différent du fichier TAG" +
                                                    "créer depuis le programme\nEjection de la clé");
                                        });
                                        try {
                                            Thread.sleep(750);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        diff.set(true);
                                        break;
                                    }
                                }
                            }
                            //Fermeture des flux de lecture de fichier
                            cryptedscan.close();
                            normalscan.close();
                        } catch (GeneralSecurityException e) {
                            Platform.runLater(() -> {
                                lbl1.setText(lbl1.getText() + "\nLa fichier TAG de la clé a été modifié mannuellement\nEjection de la clé");
                            });
                            try {
                                Thread.sleep(750);
                            } catch (InterruptedException e2) {
                                throw new RuntimeException(e2);
                            }
                            diff.set(true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    //Suppression des fichiers temporaires
                    try {
                        FileUtils.cleanDirectory(new File(System.getProperty("user.dir") + "\\tmp"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    //Si tout ce passe bien, on peut utiliser la clé
                    if (!diff.get()) {
                        Platform.runLater(() -> {
                            lbl1.setText(lbl1.getText() + "\nAucune différence détecté, bonne continuation ^^");
                        });
                        try {
                            this.countdownWithoutEject();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        usb_connected.add(root);
                    } else {
                        try {
                            this.countdown(root);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    private String getAttribut(File decryptedTag,String attr) throws FileNotFoundException {
        String res = "";
        Scanner scan = new Scanner(decryptedTag);
        while (scan.hasNextLine()) {
            res = scan.nextLine();
            if(res.startsWith(attr+" : ")){
                res = res.substring(res.indexOf(':')+1).trim();
                break;
            }
        }
        scan.close();
        return res;
    }

    private void countdownWithoutEject() throws InterruptedException {
            int j;
            for (j = 10; j > 0; j--) {
                int finalJ = j;
                Platform.runLater(() -> lbl2.setText("Ce méssage sera réinitialisé dans : " + finalJ + " secondes"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if(j==0){
                Platform.runLater(() -> {
                    lbl2.setText("");
                    stage.setIconified(true);
                });
            }
    }

    private void countdown(File root) throws InterruptedException {
        int count = 1;
        int j;
        //Ejection de la clé usb
        Platform.runLater(() -> {
            try {
                lbl1.setText(lbl1.getText() + "\n" + USBDetector.eject(root));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        count--;

        //doit attendre la fin de l'éjection (ce qu'il y a juste au dessus)
        while(count==1){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (j = 10; j > 0; j--) {
            int finalJ = j;
            Platform.runLater(() -> lbl2.setText("Ce méssage sera réinitialisé dans : " + finalJ + " secondes"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(j==0){
            Platform.runLater(() -> {
                lbl2.setText("");
                stage.setIconified(true);
            });
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
