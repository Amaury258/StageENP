package com.example.projet_heimdall.v2;

import com.example.projet_heimdall.v2.USBDetector;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class HeimdallEjector extends Task<Void> {

    private Stage stage,newStage;
    private Label lbl1,lbl2;
    public HeimdallEjector(Stage stage, Label lbl1, Label lbl2){
        this.stage = stage;
        this.lbl1 = lbl1;
        this.lbl2 = lbl2;
    }

    public synchronized Void call() throws IOException, InterruptedException {
        File server = new File("\\\\10.25.47.99\\HeimdallServeur");
        //tant qu'il est impossible de se connecter au serveur
        //while(!new File("C:\\Users\\AMAURY\\Desktop\\ara").exists()){
        while(!server.exists()){
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File[] newRoots = File.listRoots();
            //parcours des disques
            for (File root : newRoots) {
                String desc = fsv.getSystemTypeDescription(root);
                if(desc != null) {
                    boolean isusb = fsv.isDrive(root) && desc.contains("USB");
                    boolean isexternaldisk = !isInternalDisk(root) && !desc.contains("USB") && !root.getAbsolutePath().contains("C");
                    if (isusb || isexternaldisk) {
                        newStage = stage;
                        Platform.runLater(() -> {
                            newStage.show();
                            newStage.setAlwaysOnTop(true);
                            lbl1.setText("Périphérique USB "+root.getAbsolutePath()+" détecté");
                        });
                        try{
                            Thread.sleep(1000);
                        } catch (InterruptedException e){
                            System.out.println(e);
                        }
                        Platform.runLater(() -> lbl1.setText(lbl1.getText()+"\nConnection avec le serveur indisponible"));
                        try{
                            Thread.sleep(1000);
                        }catch (InterruptedException e){
                            System.out.println(e);
                        }
                        Platform.runLater(() -> lbl1.setText(lbl1.getText()+"\nÉjection du périphérique"));
                        //ejection du périphérique
                        countdown(root);
                    }
                }
            }
        }
        System.exit(1);
        return null;
    }

    private synchronized void countdown(File root) throws InterruptedException {
        AtomicInteger j = new AtomicInteger(0);
        AtomicInteger countdown = new AtomicInteger(1);
        Platform.runLater(() -> {
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
            pauseTransition.setOnFinished(actionEvent -> {
                //Ejection de la clé usb
                try {
                    Process proc = Runtime.getRuntime().exec(System.getProperty("user.dir")+"\\ejecter.cmd "+root.getAbsolutePath());
                    Scanner sc = new Scanner(proc.getInputStream(), StandardCharsets.UTF_8);
                    while(sc.hasNextLine()){
                        lbl1.setText(lbl1.getText()+"\n"+sc.nextLine());
                    }
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


        for (j.set(10); j.get() > 0; j.getAndDecrement()) {
            Platform.runLater(() -> {
                lbl2.setText("Ce méssage sera réinitialisé dans : " + j.get() + " secondes");
            });
            Platform.requestNextPulse();
            Thread.sleep(1000);
        }

        if (j.get() == 0) {
            Platform.runLater(() -> {
                lbl1.setText("");
                lbl2.setText("");
                newStage.hide();
                newStage = null;
            });
            Platform.requestNextPulse();
        }
    }

    public static boolean isInternalDisk(File f) throws IOException {
        String filename = f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-1);
        boolean isinternal = true;
        Process process = Runtime.getRuntime().exec("wmic logicaldisk where \"DeviceID='"+filename+"'\" assoc /assocclass:Win32_LogicalDiskToPartition");
        Scanner sc = new Scanner(process.getInputStream());
        String partitionStr = "";
        while(sc.hasNextLine()){
            String str = sc.nextLine();
            if(!str.isEmpty()) {
                if(str.contains("Disk #")) {
                    // Extraire la sous-chaîne "Disk #0, Partition #1" de la variable str
                    partitionStr = str.substring(str.indexOf("#")+1, str.indexOf(","));
                }
            }
        }

        if(!partitionStr.isEmpty()) {
            process = Runtime.getRuntime().exec("wmic diskdrive where Index=" +
                    Integer.parseInt(partitionStr) + " get MediaType");
            sc = new Scanner(process.getInputStream());
            while (sc.hasNextLine()) {
                String str = sc.nextLine();
                if (!str.contains("MediaType")) {
                    if (!str.contains("Fixed")) {
                        isinternal = false;
                    }
                }
            }
        }
        return isinternal;
    }
}
