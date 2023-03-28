package v2;

import javafx.scene.control.Label;

import java.io.File;

public class ConnectedThread extends Thread{
    private File root;

    public ConnectedThread(File r){
        this.root = r;
    }

    @Override
    public void run() {
        while(contains(File.listRoots(),root)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.interrupt();
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
