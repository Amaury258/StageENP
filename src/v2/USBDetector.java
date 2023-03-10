package v2;

//import de librairies externe
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

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