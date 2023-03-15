package test;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class test3 {
    public static void main(String[] args) throws IOException, JDOMException {
        try {
            // Créer un WatchService pour surveiller le dossier
            WatchService watchService = FileSystems.getDefault().newWatchService();

            Process process = Runtime.getRuntime().exec("wmic useraccount where name='AMAURY' get sid");
            BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
            bfr.readLine();
            bfr.readLine();
            String uid = bfr.readLine().replace(" ","");

            File folder = new File("C:\\ProgramData\\Bitdefender\\Desktop\\Profiles\\Logs\\"+uid);
            File max = null;
            for(File f : folder.listFiles()){
                if(max == null){
                    max = f;
                } else {
                    if (f.lastModified() > max.lastModified()) {
                        max = f;
                    }
                }
            }
            Path folderPath = Paths.get(max.getAbsolutePath());
            //Path folderPath = Paths.get("C:\\ProgramData\\Bitdefender\\Desktop\\Profiles\\Logs\\"+uid+"\\db7af1a4-db8-4f42-903a-9d200e40d03c");
            folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            // Attendre les événements de création de fichier
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    // Vérifier si l'événement est un événement de création de fichier
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        // Récupérer le nom du fichier créé
                        Path filePath = (Path) event.context();
                        String fileName = filePath.toString();

                        System.out.println(new SimpleDateFormat().format(new File(fileName).lastModified()));

                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }



    }
}
