package com.example.projet_heimdall.v2;

//import de librairie externe
import javafx.animation.PauseTransition;
import javafx.util.Duration;

//import de librairies standard de java
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineCliente extends Task<Void> {

    private Label lbl1, lbl2;
    private Stage stage,newStage;
    private boolean cd;
    public MachineCliente(Stage stage, Label lbl, Label lbl2){
        this.stage = stage;
        this.lbl1 = lbl;
        this.lbl2 = lbl2;
        cd = false;
    }
    public synchronized Void call() throws Exception {
        //Liste des clé usb connecté au pc
        ArrayList<File> usb_connected = new ArrayList<>();
        ArrayList<String> disabled = new ArrayList<>();
        while(true){
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File[] newRoots = File.listRoots();
            Iterator<File> iter = usb_connected.iterator();
            Iterator<String> iterphone = disabled.iterator();
            //Vérifier si les clés usb de la liste sont toujours connecté
            while (iter.hasNext()) {
                File f = iter.next();
                if (!contains(newRoots, f)) {
                    iter.remove(); // supprimer l'élément en utilisant l'itérateur
                }
            }

            //telephone qui marche pas
            ArrayList<String> isdisabled = new ArrayList<>();
            Process disproc = Runtime.getRuntime().exec("wmic path Win32_PnpEntity where \"Service like '%mtp%'\" get DeviceID");
            Scanner sc = new Scanner(disproc.getInputStream());
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                if(!line.contains("DeviceID") && !line.isEmpty()){
                    isdisabled.add(line.replace(" ",""));
                }
            }

            while(iterphone.hasNext()){
                String str = iterphone.next();
                if(!isdisabled.contains(str)){
                    iterphone.remove();
                }
            }

            phone_connect(disabled);

            //parcours des disques
            for (File root : newRoots) {
                boolean isusb = fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB") && !usb_connected.contains(root);
                boolean isdisk = !usb_connected.contains(root) && !USBDetector.isInternalDisk(root) && !fsv.getSystemTypeDescription(root).contains("USB");
                if (isusb || isdisk) {
                    File tmp = null;
                    File tag = null;

                    newStage = stage;

                    if(!new File(System.getProperty("user.dir")+"\\tmp").exists()){
                        new File(System.getProperty("user.dir")+"\\tmp").mkdir();
                    }

                    AtomicBoolean diff = new AtomicBoolean(false);
                    Platform.runLater(() -> {
                        newStage.setAlwaysOnTop(true);
                        newStage.show();
                        // la clé USB a été trouvée, faites quelque chose ici
                        lbl1.setText(lbl1.getText()+"Périphérique USB détecté : " + root.getAbsolutePath());
                    });
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    //Recuperation du tag crypté
                    File crypted_tag = new File(root.getAbsolutePath() + "System Volume Information\\TAG");

                    if (!crypted_tag.exists()) {
                        diff.set(true);
                    } else {
                        try {

                            tmp = File.createTempFile("uncrypted", "",new File(System.getProperty("user.dir")+"\\tmp"));


                            //prendre l'id de la clé, son nom et sa lettre de lecteur, hasher le tout
                            Scanner scanid = new Scanner(new File(root.getAbsolutePath() + "System Volume Information\\IndexerVolumeGuid"));
                            String hashkey = scanid.nextLine();
                            scanid.close();

                            hashkey = HashUtils.hashString(hashkey, "SHA-256");

                            File decrypted_tag = HashUtils.decrypt(crypted_tag, tmp, hashkey.getBytes());

                            //Creation du tag temporaire
                            tag = USBDetector.makeTAG(root);

                            if(decrypted_tag.length() == tag.length()) {

                                //Lecture du fichier TAG (déchiffré)
                                Scanner normalscan = new Scanner(tag);
                                Scanner cryptedscan = new Scanner(decrypted_tag);
                                String line1, line2;

                                //Lecture de tout le fichier TAG (déchiffré et par la methode makeTAG)
                                normalscan.nextLine();
                                cryptedscan.nextLine();
                                while (normalscan.hasNextLine() && cryptedscan.hasNextLine()) {
                                    line1 = normalscan.nextLine();
                                    line2 = cryptedscan.nextLine();
                                    //Si les lignes sont différentes, on ejecte
                                    if (!line1.equals(line2)) {
                                        diff.set(true);
                                        break;
                                    }
                                }
                                //Fermeture des flux de lecture de fichier
                                cryptedscan.close();
                                normalscan.close();
                            } else {
                                diff.set(true);
                            }

                        } catch (GeneralSecurityException e) {
                            diff.set(true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    //Si tout ce passe bien, on peut utiliser la clé
                    if (!diff.get()) {
                        Platform.runLater(() -> {
                            lbl1.setText(lbl1.getText() + "\nAucune différence détectée, bonne continuation ^^");
                        });
                        countdownWithoutEject();
                        cd = false;
                        usb_connected.add(root);
                    } else {
                        Platform.runLater(() -> {
                            lbl1.setText(lbl1.getText()+"\nPériphérique USB Invalide ! >=(");
                        });
                        try {
                            this.countdown(root);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        cd = false;
                    }
                    for(File f:new File(System.getProperty("user.dir")+"\\tmp").listFiles()){
                        f.delete();
                    }
                }
            }
            if(cd){
                countdownWithoutEject();
            }
        }
        //System.exit(1);
        //return null;
    }

    private synchronized void phone_connect(ArrayList<String> disabled) throws IOException {
        Process process = Runtime.getRuntime().exec("wmic path Win32_PnPEntity where \"Service like '%MTP%'\" get DeviceID");
        Scanner proc = new Scanner(process.getInputStream());
        ArrayList<String> phones_connected = new ArrayList<>();
        while(proc.hasNextLine()){
            String line = proc.nextLine();
            if(!line.contains("DeviceID") && !line.isEmpty()) {
                phones_connected.add(line.replace(" ",""));
            }
        }

        if(!phones_connected.isEmpty()) {
            for (String str : phones_connected) {
                if (!disabled.contains(str)) {
                    cd = true;
                    Process procverif = Runtime.getRuntime().exec("pnputil /enum-devices /instanceid \"" + str + "\"");

                    String name = "";

                    Scanner sc = new Scanner(procverif.getInputStream());

                    boolean isActivated = true;
                    while (isActivated && sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (line.contains("Description")) {
                            name = line.split(":")[1].trim();
                        } else if (line.contains("Statut : ")) {
                            if (line.contains("Activé")) {
                                break;
                            } else {
                                isActivated = false;
                            }
                        }
                    }

                    if (isActivated) {
                        //Process process1 = Runtime.getRuntime().exec("pnputil /disable-device " + str);
                        //afficher un message
                        String finalName = name;
                        newStage = stage;
                        Platform.runLater(() -> {
                            newStage.show();
                            newStage.setAlwaysOnTop(true);
                            lbl1.setText(lbl1.getText() + "\nTéléphone \"" + finalName + "\" détecté");
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Platform.runLater(() -> lbl1.setText(lbl1.getText() + "\nTéléphone \"" + finalName + "\" désactivé\n"));
                        disabled.add(str);
                    } else {
                        disabled.add(str);
                    }
                }
            }
        }
    }

    private synchronized void countdownWithoutEject() throws InterruptedException {
        AtomicInteger j = new AtomicInteger(0);
            for (j.set(5); j.get() > 0; j.getAndDecrement()) {
                Platform.runLater(() -> lbl2.setText("Ce méssage sera réinitialisé dans : " + j.get() + " secondes"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            Platform.requestNextPulse();

            if(j.get() ==0){
                Platform.runLater(() -> {
                    lbl1.setText("");
                    lbl2.setText("");
                    newStage.hide();
                });
            }
            cd = false;
    }

    private synchronized void countdown(File root) throws InterruptedException {
        AtomicInteger j = new AtomicInteger(0);
        AtomicInteger countdown = new AtomicInteger(1);
        Platform.runLater(() -> {
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
            pauseTransition.setOnFinished(actionEvent -> {
                //Ejection de la clé usb
                try {
                    lbl1.setText(lbl1.getText() + "\n"+USBDetector.eject(root)+"\nVeuillez faire analyser votre périphérique USB par la machine blanche.");
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


        for (j.set(5); j.get() > 0; j.getAndDecrement()) {
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
            });
            Platform.requestNextPulse();
        }
    }

    /**
     * Methode pour vérifier si un objet File est dans un tavbleau de File
     *
     * @param roots
     * @param root
     * @return
     */
    private static boolean contains(Object[] roots, Object root) {
        for (Object file : roots) {
            if (file.equals(root)) {
                return true;
            }
        }
        return false;
    }
}
