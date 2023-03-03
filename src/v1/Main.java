package v1;/*
 * USB Analysor
 * Version 1.0
 * Par: Amaury DEMARQUE
 *      Stagiaire à l'École Nationale de Police de Montbéliard
 */

 import javax.management.*;

 import Exceptions.*;
 
 import java.lang.management.ManagementFactory;
 import java.util.*;
 
 public class Main {
     /**
      * Prendre la liste des périphériques usb connectés au pc
      * controler si le périphérique est sur la base des périphériques autorisés
      * trois cas: 
      *         la base des périphériques autorisés est une bdd -> requête sql
      *         la base des périphériques autorisés est une liste globale (pour toute machine) -> trouver la liste
      *         la base des périphériques autorisés est une liste faite par l'entreprise -> demander la liste
      * si oui, envoyer le périphérique à l'anti-virus
      * si non, ????
      */
     public static void main(String[] args) {
         try {
             //on prend la liste des périphériques usb connectés au pc
             MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
             Set<ObjectName> objs = mbsc.queryNames(new ObjectName("javax.usb.*:*"), null);
 
             //on vérifie si la liste est vide
             if(objs.isEmpty()){
                 throw new AucunPeripheriqueException("Aucun périphérique USB détecté");
             } else {
                 //on parcourt la liste des périphériques usb
                 for (ObjectName obj : objs) {
                     System.out.println("Périphérique USB détecté : " + obj.getCanonicalName());
                     //on verifie si le périphérique est dans la liste des périphériques autorisés
                     if(obj.getCanonicalName().contains("vid=0x1a86&pid=0x7523")){
                         System.out.println("Périphérique USB autorisé");
                         //On envoie l'objet à l'anti-virus
                         /*if(antivirus.detecteVirus(obj)){
                             throw new VirusDetecte("Virus détecté");
                         } else {
                             System.out.println("Aucun virus détecté");
                             //On lance le petit programme qui nous demande ce qu'on veut faire (ouvrir dans l'explorateur, etc...)
                         }*/
                     }else{
                         throw new NonAutoriseException("Périphérique USB non autorisé");
                     }
                 }
             }
         } catch (Exception e) {
             System.out.println(e.getMessage());
             //On ejecte le périphérique si une exception est levée
         }
        
     }
 }