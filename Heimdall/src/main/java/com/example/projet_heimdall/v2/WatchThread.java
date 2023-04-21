package com.example.projet_heimdall.v2;

import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;

public class WatchThread extends Thread{

    private WatchService watchService;

    public WatchThread(WatchService ws) throws IOException {
        this.watchService = ws;

        //Chemin ver le dossier Logs
        String filename = "C:\\ProgramData\\Bitdefender\\Desktop\\Profiles\\Logs";

        //Recuperation du sid de l'utilisateur (ici c'est AMAURY mais sur les autres machines il faudrat utiliser System.getProperty("user.name")
        Process process = Runtime.getRuntime().exec("wmic useraccount where name='"+System.getProperty("user.name")+"' get sid");
        BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        bfr.readLine();
        bfr.readLine();
        String uid = bfr.readLine().replace(" ", "");

        //Attend que l'antivirus finisse d'analyser la clé (ici on attend qu'un nouveau fichier log soit créé)
        File logfolder = new File(filename + "\\" + uid);
        Path folderPath = Paths.get(logfolder.getAbsolutePath());
        folderPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    @Override
    public void run() {
        try {
            while (!waitForNewFile(watchService)) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e){
            ;
        }
        this.interrupt();
    }

    private boolean waitForNewFile(WatchService ws) throws InterruptedException {
        boolean res = false;
        WatchKey key = ws.take();
        for (WatchEvent<?> event : key.pollEvents()) {
            // Vérifier si l'événement est un événement de création de fichier
            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                return true;
            }
        }
        key.reset();
        return res;
    }
}
