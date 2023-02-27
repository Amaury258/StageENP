import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;

public class Main {
    /**
     * Prendre la liste des périphériques usb connectés au pc
     * controler si le périphérique est sur la base des périphériques autorisés
     * si oui, envoyer le périphérique à l'anti-virus
     * si non, ????
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objs = mbsc.queryNames(new ObjectName("javax.usb.*:*"), null);
            if(objs.isEmpty()){
                throw new Exception("Aucun périphérique USB détecté");
            }
            for (ObjectName obj : objs) {
                System.out.println("Périphérique USB détecté : " + obj.getCanonicalName());
                if(obj.getCanonicalName().contains("vid=0x1a86&pid=0x7523")){
                    System.out.println("Périphérique USB autorisé");
                    //envoyer l'objet à l'anti-virus
                    /*if(antivirus.detecteVirus(obj)){
                        throw new VirusDetecte("Virus détecté");
                    } else {
                        System.out.println("Aucun virus détecté");
                    }*/

                }else{
                    throw new NonAutoriseException("Périphérique USB non autorisé");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //ejecter la clé usb
        }
       
    }
}