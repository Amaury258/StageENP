package test;

import java.io.*;
import java.util.Scanner;


public class lestestsdemomo {
    public static void main(String[] args) throws Exception {
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
                if(!fini){
                    for (File oldRoot : roots) {
                        if (oldRoot.getAbsolutePath().equals(root.getAbsolutePath()) && !oldRoot.getAbsolutePath().equals("C:\\")) {
                            found = true;
                            fini = true;
                            break;
                        }
                    }
                }

                if (found || !contains(roots,root)) {
                    System.out.println("Clé USB trouvé : "+root.getAbsolutePath());

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

    public static void lockDirectory(File file) throws IOException {

    }

    public static void unlockDirectory(File src) throws IOException {

    }


    private static boolean contains(File[] roots, File root) {
        for (File file : roots) {
            if (file.equals(root)) {
                return true;
            }
        }
        return false;
    }
}
