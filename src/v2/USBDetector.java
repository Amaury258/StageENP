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
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class USBDetector {

    public static File makeTAG(File root) throws IOException, ExecutionException, InterruptedException {
        ArrayList<String> lines = new ArrayList<>();
        File tagnormal = null;
        try {
            tagnormal = File.createTempFile("tagnormal","",new File(System.getProperty("user.dir")+"\\tmp"));
            boolean virus = false;

            lines.add("Virus : "+virus);
            //SUID 8-4-4-4-12
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
            System.out.println(e.getStackTrace());
        }
        return tagnormal;
    }

    public static File makeTAGAntivirus(File root) throws IOException, ExecutionException, InterruptedException {
        ArrayList<String> lines = new ArrayList<>();
        File tagnormal = null;
        try {
            tagnormal = File.createTempFile("tagnormal","",new File(System.getProperty("user.dir")+"\\tmp"));
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

    public static boolean getResultAnalyse() throws IOException, JDOMException {
        boolean res = false;
        String filename = "C:\\ProgramData\\Bitdefender\\Desktop\\Profiles\\Logs";

        Process process = Runtime.getRuntime().exec("wmic useraccount where name='AMAURY' get sid");
        BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        bfr.readLine();
        bfr.readLine();
        String uid = bfr.readLine().replace(" ","");

        File log = getLastLog(new File(filename+"\\"+uid));

        System.out.println("uwu1");

        // Chargement du fichier XML avec JDOM
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(log);

        // Accès à l'élément racine
        Element racine = document.getRootElement();

        List<Element> typeSummaries = racine.getChild("ScanSummary").getChildren("TypeSummary");

        for (Element typeSummary : typeSummaries) {
            String infected = typeSummary.getAttributeValue("infected");
            String suspicious = typeSummary.getAttributeValue("suspicious");

            if(Integer.parseInt(infected) != 0 && Integer.parseInt(suspicious) != 0){
                res = true;
                break;
            }
        }
        return res;
    }

    public static File getLastLog(File file){
        File res = null;
        File max = null;
        for(File f : file.listFiles()){
            System.out.println(f.getAbsolutePath());
            if(max == null){
                max = f;
            } else {
                if(max.lastModified()<f.lastModified()){
                    max = f;
                }
            }
        }

        for(File f : max.listFiles()){
            if(f.lastModified()==max.lastModified()){
                res = f;
            }
        }
        return res;
    }

    private static ArrayList getListFiles(File file, ArrayList al){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(File f : file.listFiles()){
            if(!f.isHidden()){
                if(f.isDirectory()){
                    ArrayList<String> newal = new ArrayList<>();
                    newal = getListFiles(f,newal);
                    al.add(HashUtils.hashString(f.getPath(),"SHA-256"));
                    //al.add(f.getPath());
                    for(String str : newal) {
                        al.add(str);
                    }
                } else {
                    String str = HashUtils.hashString(f.getPath()+" - "+sdf.format(f.lastModified()),"SHA-256");
                    //String str = f.getPath()+" - "+sdf.format(f.lastModified());
                    al.add(str);
                }
            }
        }
        return al;
    }

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