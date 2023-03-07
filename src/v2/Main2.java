package v2;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main2 {

    public static void main(String[] args) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:MM:ss");
        Scanner sc = new Scanner(System.in);
        File[] roots = File.listRoots();
        HashMap<String, File> usb_connected = new HashMap<>();
        int i = 0;
        while (true) {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            if(i==0){
                System.out.println("Recherche de clé USB");
                i++;
            }
            File[] newRoots = File.listRoots();
            for (File root : newRoots) {
                if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB") && !usb_connected.containsValue(root)) {
                    // la clé USB a été trouvée, faites quelque chose ici
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());
                    usb_connected.put(root.getAbsolutePath(), root);
                    System.out.println("Appuyez sur \"Entrée\" pour éjecter la clé USB.");
                    boolean index = false;
                    boolean last_modified = false;
                    boolean hashcode = false;
                    try {
                        /*
                            ->IndexerVolumeGuid
                            ->lastModified()
                            ->HashCode
                         */
                        File TAG = new File(root.getAbsolutePath()+"System Volume Information\\TAG");
                        List<String> lines = new ArrayList<>();
                        if(!TAG.exists()){
                            FileWriter fw = new FileWriter(TAG,true);
                            fw.write("IndexerVolumeGuid : \n");
                            fw.write("Last Modified (long) : \n");
                            fw.write("HashCode : ");
                            fw.close();
                        }
                        Scanner scan = new Scanner(TAG);
                        while(scan.hasNextLine()){
                            String line = scan.nextLine();
                            if(line.contains("Last Modified (long) : ")){
                                String strmodif = line.substring(line.indexOf(':')+1).trim();
                                if(!strmodif.isEmpty()){
                                    long lastmodifvalue = Long.parseLong(strmodif);
                                    long lastModif = analyseAll(root);
                                    if(lastmodifvalue != lastModif){
                                        last_modified = true;
                                    }
                                } else {
                                    line = "Last Modified (long) : "+analyseAll(root);
                                    lines.add(line);
                                }

                            }
                            if(line.contains("IndexerVolumeGuid : ")){
                                String indexvalue = line.substring(line.indexOf(':')+1).trim();
                                File indexFile = new File(root.getAbsolutePath()+"System Volume Information\\IndexerVolumeGuid");
                                FileReader fileReader = new FileReader(indexFile, Charset.forName(getEncodage(indexFile)));
                                BufferedReader reader = new BufferedReader(fileReader);
                                String lineindex = reader.readLine();
                                reader.close();
                                fileReader.close();
                                if(!indexvalue.isEmpty()){
                                    if(indexvalue.equals(lineindex)){
                                        index = true;
                                    }
                                } else {
                                    line = "IndexerVolumeGuid : "+lineindex;
                                    lines.add(line);
                                }
                            }
                            if(line.contains("HashCode : ")){
                                //faire un truc avec le hash code
                            }
                        }
                        scan.close();

                        if(!lines.isEmpty()){
                            FileWriter fw = new FileWriter(TAG,false);
                            for(String str : lines){
                                fw.write(str+"\n");
                            }
                            fw.close();
                        }


                        sc.nextLine();
                        //if virus
                        if(!index || !last_modified || !hashcode /*|| virus*/){
                            eject(root);
                        }
                        i--;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            roots = newRoots;

            for(String key : usb_connected.keySet()){
                USBThread ut = new USBThread(usb_connected.get(key));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String getEncodage(File file) throws IOException {
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

    private static long analyseAll(File file){
        long max_modif = 0;
        for(File f : file.listFiles()){
            if(!f.getName().equals("System Volume Information")){
                if(f.isDirectory()){
                    long tmp = analyseAll(f);
                    if(max_modif<tmp){
                        max_modif = tmp;
                    }
                } else {
                    if(max_modif<f.lastModified()){
                        max_modif = f.lastModified();
                    }
                }
            }
        }
        return max_modif;
    }

    private static void eject(File drive) throws IOException {
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