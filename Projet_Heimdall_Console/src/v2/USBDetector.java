package v2;

//import de librairies externe
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
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
import java.util.concurrent.*;

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
            boolean virus = false;
            lines.add("Virus : "+virus);

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
    public static File makeTAGAntivirus(File root) {
        ArrayList<String> lines = new ArrayList<>();
        File tagnormal = null;
        try {
            tagnormal = File.createTempFile("tagnormal","",new File(System.getProperty("user.dir")+"\\tmp"));

            //Ici on vas chercher le résultat de l'analyse de la clé USB (elle est faite automatiquement par BitDefender)
            boolean virus = getResultAnalyse();
            lines.add("Virus : "+virus);

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
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return tagnormal;
    }

    /**
     * Methode pour récuperer le résultat de l'analyse de la clé USB
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JDOMException
     */
    public static boolean getResultAnalyse() throws IOException, InterruptedException, JDOMException {
        boolean res = false;

        // Créer un WatchService pour surveiller le dossier
        WatchService watchService = FileSystems.getDefault().newWatchService();

        //Chemin ver le dossier Logs
        String filename = "C:\\ProgramData\\Bitdefender\\Desktop\\Profiles\\Logs";

        //Recuperation du sid de l'utilisateur (ici c'est AMAURY mais sur les autres machines il faudrat utiliser System.getProperty("user.name")
        Process process = Runtime.getRuntime().exec("wmic useraccount where name='AMAURY' get sid");
        BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        bfr.readLine();
        bfr.readLine();
        String uid = bfr.readLine().replace(" ","");

        //Attend que l'antivirus finisse d'analyser la clé (ici on attend qu'un nouveau fichier log soit créé)
        File logfolder = getLogFolder(new File(filename+"\\"+uid));
        Path folderPath = Paths.get(logfolder.getAbsolutePath());
        folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        while(!waitForNewFile(watchService)){
            Thread.sleep(1000);
        }
        File log = getLastLog(getLogFolder(new File(filename+"\\"+uid)));

        // Chargement du fichier XML avec JDOM
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(log);

        // Accès à l'élément racine
        Element racine = document.getRootElement();
        List<Element> typeSummaries = racine.getChild("ScanSummary").getChildren("TypeSummary");
        for (Element typeSummary : typeSummaries) {
            String infected = typeSummary.getAttributeValue("infected");
            String suspicious = typeSummary.getAttributeValue("suspicious");
            if(!infected.equals("0") || !suspicious.equals("0")){
                System.out.println(infected+" - "+suspicious);
                res = true;
                break;
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
    private static boolean waitForNewFile(WatchService ws) throws InterruptedException {
        boolean res = false;
        WatchKey key = ws.take();
        for (WatchEvent<?> event : key.pollEvents()) {
            // Vérifier si l'événement est un événement de création de fichier
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                // Récupérer le nom du fichier créé
                Path filePath = (Path) event.context();
                String fileName = filePath.toString();

                res = true;

            }
        }
        key.reset();
        return res;
    }

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
    public static void eject(File drive) throws IOException {
        // Récupérer la lettre de lecteur de la clé USB
        String driveLetter = drive.getAbsolutePath().substring(0,1);

        String batchFilePath = System.getProperty("user.dir") + "\\src\\v2\\ejecter.cmd";
        Process process = Runtime.getRuntime().exec(" "+batchFilePath+" "+driveLetter+" "+System.getProperty("user.dir"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        System.out.println(output);
    }
}