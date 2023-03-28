package com.example.projet_heimdall.v2;

//import de librairies externe
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

//import de librairies standard de java
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class USBDetector {

    /**
     * Créateur du TAG Temporaire non chiffré pour pouvoir le comparer avec le fichier TAG de la clé qui lui est chiffré
     *
     * @param root
     * @return
     */
    public static File makeTAG(File root) {
        ArrayList<String> lines = new ArrayList<>();
        File tagnormal = null;
        try {
            //Création du fichier temporaire non chiffré
            tagnormal = File.createTempFile("tagnormal","",new File(System.getProperty("user.dir")+"\\tmp"));

            //Si la clé usb est passé sur la machine blanche, il ne devrait pas y avoir de virus
            //lines.add("Virus : "+virus);

            //On récupere l'id de la clé USB
            File indexFile = new File(root.getAbsolutePath() + "System Volume Information\\IndexerVolumeGuid");
            FileReader fileReader = new FileReader(indexFile, Charset.forName(getEncodage(indexFile)));
            BufferedReader reader = new BufferedReader(fileReader);
            lines.add(reader.readLine());
            reader.close();
            fileReader.close();

            //On récupere dans une liste les fichiers présent sur la clé USB
            lines = getListFiles(root, lines);

            if (!lines.isEmpty()) {
                FileWriter writer = new FileWriter(tagnormal, false);
                for (String str : lines) {
                    writer.write(str + "\n");
                }
                writer.close();

                while(!Files.isWritable(tagnormal.toPath())){
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        return tagnormal;
    }

    /**
     * Création du TAG coté Machine blanche qui sera chiffré par la suite
     * @param root
     * @return
     */
    public static synchronized File makeTAGAntivirus(File root, Label label, Stage tuto) {
        Platform.runLater(() -> label.setText(label.getText() + "\nEn attente du résultat de l'analyse"));
        ArrayList<String> lines = new ArrayList<>();
        File tagnormal = null;
        try {
            tagnormal = File.createTempFile("tagnormal","",new File(System.getProperty("user.dir")+"\\tmp"));

            int virus = getResultAnalyseDisk(tuto,root);

            if(virus==0) {
                Platform.runLater(() -> label.setText(label.getText()+"\nAnalyse terminé"));
                Thread.sleep(750);
                Platform.runLater(() -> label.setText(label.getText()+"\nCréation du TAG"));
                Thread.sleep(750);
                File indexFile = new File(root.getAbsolutePath() + "System Volume Information\\IndexerVolumeGuid");
                FileReader fileReader = new FileReader(indexFile, Charset.forName(getEncodage(indexFile)));
                BufferedReader reader = new BufferedReader(fileReader);
                lines.add(reader.readLine());
                reader.close();
                fileReader.close();

                lines = getListFiles(root, lines);

                if (!lines.isEmpty()) {
                    FileWriter writer = new FileWriter(tagnormal, false);
                    for (String str : lines) {
                        writer.write(str + "\n");
                    }
                    writer.close();

                    while(!Files.isWritable(tagnormal.toPath())){
                        Thread.sleep(1000);
                    }
                }
            } else if(virus==1){
                return new File("");
            } else {
                return null;
            }

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Erreur en ligne : "+e.getStackTrace()[0].getLineNumber());
        }
        return tagnormal;
    }

    public static boolean isInternalDisk(File f) throws IOException {
        String filename = f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-1);
        boolean isinternal = false;
        Process process = Runtime.getRuntime().exec("wmic logicaldisk where \"DeviceID='"+filename+"'\" assoc /assocclass:Win32_LogicalDiskToPartition");
        BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String str;
        String partitionStr = "";
        while((str = bfr.readLine()) !=null){
            if(!str.isEmpty()) {
                if(str.contains("Disk #")) {
                    // Extraire la sous-chaîne "Disk #0, Partition #1" de la variable str
                    partitionStr = str.substring(str.indexOf("#")+1, str.indexOf(","));
                }
            }
        }
        bfr.close();

        process = Runtime.getRuntime().exec("wmic diskdrive where Index="+
                Integer.parseInt(partitionStr)+" get MediaType");
        bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while((str = bfr.readLine()) != null){
            if(!str.contains("MediaType")){
                if(str.contains("Fixed")){
                    isinternal = true;
                }
            }
        }
        return isinternal;
    }

    /**
     * Methode pour récuperer le résultat de l'analyse de la clé USB
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JDOMException
     */
    public static int getResultAnalyseDisk(Stage tuto, File root) throws IOException, InterruptedException, JDOMException {
        int res = 0;


            Platform.runLater(() -> tuto.show());
            try {
                Runtime.getRuntime().exec("explorer.exe /root,::{20D04FE0-3AEA-1069-A2D8-08002B30309D}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        // Créer un WatchService pour surveiller le dossier
        WatchService watchService = FileSystems.getDefault().newWatchService();

        WatchThread wt = new WatchThread(watchService);
        ConnectedThread ct = new ConnectedThread(root);

        wt.start();
        ct.start();
        while(!wt.isInterrupted() && !ct.isInterrupted()){
            Thread.sleep(500);
            if(ct.isInterrupted()){
                res = -1;
                break;
            } else if(wt.isInterrupted()){
                break;
            }
        }


            Platform.runLater(() -> tuto.close());
            Platform.requestNextPulse();
            // trouver la fenêtre de l'explorateur de fichiers par titre et classe
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow("CabinetWClass", "Ce PC");

            // fermer la fenêtre
            User32.INSTANCE.PostMessage(hwnd, WinUser.WM_CLOSE, null, null);


        if(res == 0) {

            //Chemin ver le dossier Logs
            String filename = "C:\\ProgramData\\Bitdefender\\Desktop\\Profiles\\Logs";

            //Recuperation du sid de l'utilisateur (ici c'est AMAURY mais sur les autres machines il faudrat utiliser System.getProperty("user.name")
            Process process = Runtime.getRuntime().exec("wmic useraccount where name='AMAURY' get sid");
            BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
            bfr.readLine();
            bfr.readLine();
            String uid = bfr.readLine().replace(" ", "");

            File log = getLastLog(getLogFolder(new File(filename + "\\" + uid)));

            // Chargement du fichier XML avec JDOM
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(log);

            System.out.println(document.toString());

            // Accès à l'élément racine
            Element racine = document.getRootElement();
            if(racine.getChild("ScanSettings").getChild("ScanPaths").getChildText("path").equals(root.getAbsolutePath())) {
                List<Element> typeSummaries = racine.getChild("ScanSummary").getChildren("TypeSummary");
                for (Element typeSummary : typeSummaries) {
                    String infected = typeSummary.getAttributeValue("infected");
                    String suspicious = typeSummary.getAttributeValue("suspicious");
                    if (!infected.equals("0") || !suspicious.equals("0")) {
                        res = 1;
                        break;
                    }
                }
            } else {
                res = -1;
            }
        }
        return res;
    }

    /**
     * Methode pour obtenir le derniere log
     *
     * @param file
     * @return
     */
    private static File getLastLog(File file){
        File max = null;
        for(File f : file.listFiles()){
            if(max == null){
                max = f;
            } else {
                if(max.lastModified()<f.lastModified()){
                    max = f;
                }
            }
        }
        return max;
    }

    /**
     * Methode pour l'attente d'un nouveau fichier log
     *
     * @param ws
     * @return
     * @throws InterruptedException
     */


    /**
     * Methode pour récuperer le dossier des logs
     *
     * @param file
     * @return
     */
    public static File getLogFolder(File file){
        File max = null;
        for(File f : file.listFiles()){
            if(max == null){
                max = f;
            } else {
                if (f.lastModified() > max.lastModified()) {
                    max = f;
                }
            }
        }
        return max;
    }

    /**
     * Méthode pour récuperer la liste des fichiers d'un dossier
     * @param file
     * @param al
     * @return
     */
    private static ArrayList getListFiles(File file, ArrayList al){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(File f : file.listFiles()){
            if(!f.isHidden()){
                if(f.isDirectory()){
                    ArrayList<String> newal = new ArrayList<>();
                    newal = getListFiles(f,newal);
                    al.add(HashUtils.hashString(f.getPath(),"SHA-256"));
                    for(String str : newal) {
                        al.add(str);
                    }
                } else {
                    String str = HashUtils.hashString(f.getPath()+" - "+sdf.format(f.lastModified()),"SHA-256");
                    al.add(str);
                }
            }
        }
        return al;
    }

    /**
     * Methode pour récuperer l'encodage d'un fichier (utilisé pour le fichier IndexerVolumeGuid
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static String getEncodage(File file) throws IOException {
        String res = "";
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        CharsetDetector detector = new CharsetDetector();
        detector.setText(data);
        CharsetMatch[] matches = detector.detectAll();
        if(matches.length > 0){
            res = matches[0].getName();
        }
        return res;
    }

    /**
     * Methode pour ejecter la clé USB
     * @param drive
     * @throws IOException
     */
    public static String eject(File drive) throws IOException {
        // Récupérer la lettre de lecteur de la clé USB
        String driveLetter = drive.getAbsolutePath().substring(0,1);

        //String batchFilePath = System.getProperty("user.dir") + "\\src\\main\\java\\com\\example\\projet_heimdall\\v2\\ejecter.cmd";
        String batchFilePath = System.getProperty("user.dir")+"\\ejecter.cmd";
        System.out.println(batchFilePath);
        Process process = Runtime.getRuntime().exec(" "+batchFilePath+" "+driveLetter+" "+System.getProperty("user.dir"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        return output.toString();
    }
}