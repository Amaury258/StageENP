/*
 * USB Analysor
 * Version 2
 * Par: Amaury DEMARQUE
 * Stagiaire à l'École Nationale de Police de Montbéliard
 */

package com.example.v2;

//import de librairie standard de java
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;

import javax.swing.filechooser.FileSystemView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineBlanche extends Task {
    public Label lbl;
    public Label lbl2;
    public ImageView imageView;
    public VBox vbox;
    private int i;
    public MachineBlanche(VBox vbox, Label lbl2){
        this.lbl2 = lbl2;
        this.vbox = vbox;
        for(Node n : vbox.getChildren()){
            if(n.getClass().equals(Label.class)){
                this.lbl = (Label) n;
            }
            if(n.getClass().equals(ImageView.class)){
                this.imageView = (ImageView) n;
            }
        }
        i=0;
    }
        public Void call() throws Exception {
            int j = 0;
            while (true) {
                synchronized (this) {
                    if (isCancelled()) {
                        break;
                    }

                    if (i == 0) {
                        Platform.runLater(() -> lbl.setText("Recherche d'une clé USB"));
                        i++;
                    }

                    FileSystemView fsv = FileSystemView.getFileSystemView();
                    File[] newRoots = File.listRoots();
                    //parcours des disques
                    for (File root : newRoots) {
                        if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB") || fsv.isFloppyDrive(root)) {
                            // la clé USB a été trouvée
                            Platform.runLater(() -> {
                                vbox.getChildren().remove(imageView);
                                lbl.setText("Clé USB détecté : " + root.getAbsolutePath());
                                lbl.setText(lbl.getText() + "\nEn attente du résultat de l'analyse");
                            });
                            Platform.requestNextPulse();

                            synchronized (this) {
                                //Creation du tag
                                File tag = USBDetector.makeTAGAntivirus(root, lbl);

                                Platform.runLater(() -> lbl.setText(lbl.getText() + "\nChiffrement du TAG"));


                                //prendre l'id de la clé, son nom et sa lettre de lecteur, hasher le tout

                                Scanner scanid = new Scanner(new File(root.getAbsolutePath() + "System Volume Information\\IndexerVolumeGuid"));
                                String hashkey = scanid.nextLine();

                                scanid.close();

                                hashkey = HashUtils.hashString(hashkey, "SHA-256");

                                //Chiffrement du tag + sauvegarde sur la clé usb
                                HashUtils.encrypt(tag, root.getAbsolutePath() + "System Volume Information\\TAG",
                                        hashkey.getBytes());

                                Platform.runLater(() -> lbl.setText(lbl.getText() + "\nFichier chiffré\nÉjection de la clé USB"));

                                tag.delete();

                                Thread.sleep(1000);

                            AtomicInteger countdown = new AtomicInteger(1);
                            //Ejection de la clé usb
                            Platform.runLater(() -> {
                                try {
                                    lbl.setText(lbl.getText() + "\n" + USBDetector.eject(root));
                                    countdown.getAndDecrement();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            Platform.requestNextPulse();

                            //doit attendre la fin de l'éjection (ce qu'il y a juste au dessus)
                            while (countdown.intValue() == 1) {
                                Thread.sleep(100);
                            }

                            for (j = 10; j > 0; j--) {
                                int finalJ = j;
                                Platform.runLater(() -> lbl2.setText("Ce méssage sera réinitialisé dans : " + finalJ + " secondes"));
                                Platform.requestNextPulse();
                                Thread.sleep(1000);
                            }

                            if (j == 0) {
                                Platform.runLater(() -> {
                                    lbl2.setText("");
                                    vbox.getChildren().add(imageView);
                                });
                                Platform.requestNextPulse();
                                i--;
                            }


                            }
                        }
                    }
                }
            }
            return null;
        }
}
