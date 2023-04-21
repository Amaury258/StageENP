/*
 * USB Analysor
 * Version 2
 * Par: Amaury DEMARQUE
 * Stagiaire à l'École Nationale de Police de Montbéliard
 */

package com.example.projet_heimdall.v2;

//import de librairie standard de java
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import javax.swing.filechooser.FileSystemView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineBlanche extends Task {
    public Label lbl;
    public Label lbl2;
    public ImageView imageView;
    public VBox vbox;
    private int i;
    private Stage tuto;
    private double xOffset, yOffset;
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
        tuto = new Stage();
        tuto.initStyle(StageStyle.UNDECORATED);
        tuto.setOnCloseRequest(windowEvent -> windowEvent.consume());

        BorderPane root = new BorderPane();

        Label lbltuto = new Label("");
        lbltuto.setText(lbltuto.getText()+"\n1 - Faite un clique droit sur votre disque dur");
        lbltuto.setText(lbltuto.getText()+"\n2 - Glissez votre souris sur l'option \"Bitdefender\"");
        lbltuto.setText(lbltuto.getText()+"\n3 - Faite un clique gauche sur \"Analyser avec Bitdefender\"");
        lbltuto.setText(lbltuto.getText()+"\n4 - Si il y a une fenetre qui vous demande de choisir ce que vous voulez faire avec votre menace :");
        lbltuto.setText(lbltuto.getText()+"\n       4.1 - Si vous voulez supprimer les menaces, faite un");
        lbltuto.setText(lbltuto.getText()+"\n             clique gauche sur la liste déroulante avec");
        lbltuto.setText(lbltuto.getText()+"\n             \"Appliquer à l'élement\" et séléctionnez \"Supprimer\"");
        lbltuto.setText(lbltuto.getText()+"\n       4.2 - Si vous voulez ne rien faire (ce qui vas laisser la");
        lbltuto.setText(lbltuto.getText()+"\n             menace sur votre disque et vous empecher d'utiliser");
        lbltuto.setText(lbltuto.getText()+"\n             votre disque dur dans l'école), faite un clique gauche");
        lbltuto.setText(lbltuto.getText()+"\n             sur la liste déroulante et séléctionnez \"Ne rien faire\"");
        lbltuto.setText(lbltuto.getText()+"\n5 - Et enfin attendre la fin de l'analyse");
        Label titre = new Label("Ceci est un tuto pour analyser votre disque dur avec BitDefender");

        titre.setFont(Font.font("Arial",20));
        lbltuto.setFont(Font.font("Arial",17));

        BorderPane.setMargin(titre,new Insets(50,20,0,20));
        BorderPane.setMargin(lbltuto,new Insets(50,0,50,30));

        BorderPane.setAlignment(titre, Pos.CENTER);
        BorderPane.setAlignment(lbltuto, Pos.CENTER);

        titre.setWrapText(true);
        lbltuto.setWrapText(true);

        // Gérer le déplacement de la fenêtre avec la souris
        root.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged((MouseEvent event) -> {
            tuto.setX(event.getScreenX() - xOffset);
            tuto.setY(event.getScreenY() - yOffset);
        });

        root.setCenter(lbltuto);
        root.setTop(titre);

        tuto.setScene(new Scene(root,500,600));
    }

        public Void call() throws Exception {
            int j = 0;
            if(!new File(System.getProperty("user.dir")+"\\tmp").exists()){
                new File(System.getProperty("user.dir")+"\\tmp").mkdir();
            }
            while (true) {
                    if (isCancelled()) {
                        break;
                    }

                    if (i == 0) {
                        Platform.runLater(() -> lbl.setText("Recherche de périphérique USB"));
                        i++;
                    }

                    FileSystemView fsv = FileSystemView.getFileSystemView();
                    File[] newRoots = File.listRoots();
                    //parcours des disques
                    for (File root : newRoots) {
                        if ((fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB")) ||
                                (!USBDetector.isInternalDisk(root) && !fsv.getSystemTypeDescription(root).contains("USB"))) {
                            // la clé USB a été trouvée
                            Platform.runLater(() -> {
                                vbox.getChildren().remove(imageView);
                                lbl.setText("Périphérique USB détecté : " + root.getAbsolutePath());
                            });
                            Platform.requestNextPulse();

                                //Creation du tag
                                File tag = USBDetector.makeTAGAntivirus(root, lbl,tuto);

                                if(tag!=null){
                                    if(!tag.getAbsolutePath().equals(new File("").getAbsolutePath())) {

                                        Platform.runLater(() -> lbl.setText(lbl.getText() + "\nChiffrement du TAG"));

                                        //prendre l'id de la clé, son nom et sa lettre de lecteur, hasher le tout

                                        Scanner scanid = new Scanner(new File(root.getAbsolutePath() + "System Volume Information\\IndexerVolumeGuid"));
                                        String hashkey = scanid.nextLine();
                                        scanid.close();

                                        hashkey = HashUtils.hashString(hashkey, "SHA-256");

                                        File out = new File(root.getAbsolutePath() + "System Volume Information\\TAG");

                                        //Chiffrement du tag
                                        HashUtils.encrypt(tag, out.getAbsolutePath(),
                                                hashkey.getBytes());

                                        Platform.runLater(() -> lbl.setText(lbl.getText() + "\nFichier chiffré\nÉjection de la clé USB"));

                                        Thread.sleep(1000);
                                    } else {
                                        if(new File(root.getAbsolutePath() + "System Volume Information\\TAG").exists()) {
                                            new File(root.getAbsolutePath() + "System Volume Information\\TAG").delete();
                                        }

                                        Platform.runLater(() -> {
                                            lbl.setText(lbl.getText() + "\nVirus détecté\nEjection du périphérique USB");
                                        });

                                    }
                                } else {

                                    if(new File(root.getAbsolutePath() + "System Volume Information\\TAG").exists()) {
                                        new File(root.getAbsolutePath() + "System Volume Information\\TAG").delete();
                                    }

                                    Platform.runLater(() -> {
                                        lbl.setText("Il y a eu un probleme lors de l'analyse ou de la création du TAG");
                                    });

                                }
                            if(root.exists()) {
                            AtomicInteger countdown = new AtomicInteger(1);
                            Platform.runLater(() -> {
                                    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
                                    pauseTransition.setOnFinished(actionEvent -> {
                                        //Ejection de la clé usb
                                        try {
                                            lbl.setText(lbl.getText() + "\n"+USBDetector.eject(root));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                        countdown.getAndDecrement();
                                    });
                                    pauseTransition.play();
                            });

                            //doit attendre la fin de l'éjection (ce qu'il y a juste au dessus)
                            while (countdown.intValue() == 1) {
                                Thread.sleep(1000);
                            }
                        }
                            for (j = 10; j > 0; j--) {
                                int finalJ = j;
                                Platform.runLater(() -> {
                                    lbl2.setText("Ce méssage sera réinitialisé dans : " + finalJ + " secondes");
                                });
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

                            for(File f:new File(System.getProperty("user.dir")+"\\tmp").listFiles()){
                                f.delete();
                            }
                        }
                    }

            }
            return null;
        }
}
