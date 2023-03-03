package v2;

import java.io.*;
import java.util.List;
import java.util.Scanner;


public class Main2 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        File[] roots = File.listRoots();
        int i = 0;
        while (true) {
            if(i==0){
                System.out.println("Recherche de clé USB");
                i++;
            }
            File[] newRoots = File.listRoots();
            boolean found = false;
            boolean fini = false;
            for (File root : newRoots) {
                for (File oldRoot : roots) {
                    if (oldRoot.getAbsolutePath().equals(root.getAbsolutePath()) && !oldRoot.getAbsolutePath().equals("C:\\")) {
                        found = true;
                        break;
                    }
                }
                if (found || !contains(roots,root)) {
                    // la clé USB a été trouvée, faites quelque chose ici
                    System.out.println("Clé USB détecté : " + root.getAbsolutePath());
                    try {
                        sc.nextLine();
                        eject(root);
                        i--;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            roots = newRoots;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static boolean contains(File[] roots, File root) {
        for (File file : roots) {
            if (file.equals(root)) {
                return true;
            }
        }
        return false;
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