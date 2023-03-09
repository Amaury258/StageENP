package v2;

import java.io.*;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class USBThread extends Thread{
    private File usb;
    private boolean running;
    public USBThread(File f){
        this.usb = f;
        this.running = true;
    }

    public void run(){
        File TAG = new File(usb.getAbsolutePath()+"System Volume Information\\TAG");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(TAG.getAbsolutePath());
            bw = new BufferedWriter(fw);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(this.running){
            if(usb.exists()){
                Scanner scanner = null;
                long modif = last_modif(this.usb);
                try {
                    scanner = new Scanner(TAG);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                while(scanner.hasNextLine()){
                    String line = scanner.nextLine();

                    if(line.contains("Last Modified (long) : ")){
                        if(Long.parseLong(line.substring(line.indexOf(':')+1)) != modif){
                            line = "Last Modified (long) : "+modif;
                            try {
                                bw.write(line);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                try {
                    this.wait(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.running = false;
            }
        }
    }

    private long last_modif(File file){
        long max_modif = 0;
        for(File f : file.listFiles()){
            if(f.isDirectory()){
                long tmp = last_modif(f);
                if(max_modif<tmp){
                    max_modif = tmp;
                }
            } else {
                if(max_modif<f.lastModified()){
                    max_modif = f.lastModified();
                }
            }
        }
        return max_modif;
    }

}
