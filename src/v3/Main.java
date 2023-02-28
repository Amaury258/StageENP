package v3;

public class Main {
    public static void main(String[] args) {
        DetectUSB dUsb = new DetectUSB();

        if(dUsb.getUsbFileSet().isEmpty()){
            System.out.println("Aucun périphérique USB détecté");
        }
    }
}