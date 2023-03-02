package v2;

import java.io.*;

public class Main2 {

    public static void main(String[] args) {
        File[] roots = File.listRoots();
        int i = 0;
        while (true) {
            if(i==0){
                System.out.println("Recherche de clé USB");
                i++;
            }
            File[] newRoots = File.listRoots();
            for (File root : newRoots) {
                if (!contains(roots, root)) {
                    System.out.println("USB device inserted: " + root.getAbsolutePath());

                    // Analyse antivirus + autorisation

                    System.out.println("La clé USB n'as pas été autorisée ou contient un virus");
                    try {
                        eject(root);
                        i--;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            roots = newRoots;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        System.out.println(output.toString());


    }
}


/*
    public static void main(String[] args) {
        File[] roots = File.listRoots();

        while (true) {
            File[] newRoots = File.listRoots();
            for (File root : newRoots) {
                if (!contains(roots, root)) {
                    System.out.println("USB device inserted: " + root.getAbsolutePath());
                }
            }
            for (File root : roots) {
                if (!contains(newRoots, root)) {
                    System.out.println("USB device removed: " + root.getAbsolutePath());
                }
            }
            roots = newRoots;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
*/