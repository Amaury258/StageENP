package test;

import org.usb4java.*;

import javax.swing.filechooser.FileSystemView;
import javax.usb.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class mestest2 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        try {
            int i = 0;
            while(true){
                FileSystemView fsv = FileSystemView.getFileSystemView();
                if(i==0){
                    System.out.println("Recherche de clé USB");
                    i++;
                }
                for (File root : File.listRoots()) {
                    if (fsv.isDrive(root) && fsv.getSystemTypeDescription(root).contains("USB")) {
                        System.out.println("Clé USB détectée : " + root.getAbsolutePath());
                        lock(root);
                        //lock + analyse antivirus
                        System.out.println("Appuyez sur \"Entrée\" pour éjecter la clé USB.");
                        sc.nextLine();
                        /*if(autorisé && aucun virus)
                            rien, l'utilisateur peut utiliser la clé usb
                          else
                            la clé est éjecté
                        */

                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

    }

    public static void lock(File file) throws Exception{
        // Récupérer la lettre de lecteur de la clé USB
        String driveLetter = file.getAbsolutePath().substring(0,2);

        String batchFilePath = System.getProperty("user.dir") + "\\src\\v2\\lock.cmd";
        Process process = Runtime.getRuntime().exec(" "+batchFilePath+" "+driveLetter+" "+System.getProperty("user.dir"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        System.out.println(output);
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