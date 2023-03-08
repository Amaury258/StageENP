package v2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HashUtils {
    public static String hashString(String inputString, String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = messageDigest.digest(inputString.getBytes());
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing input string with " + algorithm, e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /*public static File encryptFile(File inputFile, File outputFile, String key) throws Exception {

        // Convertir la clé en un tableau de bytes de taille fixe
        byte[] keyBytes = key.getBytes("UTF-8");
        byte[] keyBytesFixed = new byte[16];
        System.arraycopy(keyBytes, 0, keyBytesFixed, 0, Math.min(keyBytes.length, 16));

        // Initialiser la clé de chiffrement
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytesFixed, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Ouvrir le fichier d'entrée et le fichier de sortie
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile,false))) {

            // Chiffrer le fichier
            char[] buffer = new char[4096];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                byte[] encryptedBytes = cipher.update(new String(buffer, 0, bytesRead).getBytes("UTF-8"));
                if (encryptedBytes != null) {
                    writer.write(Base64.getEncoder().encodeToString(encryptedBytes));
                }
            }
            byte[] encryptedBytes = cipher.doFinal();
            if (encryptedBytes != null) {
                writer.write(Base64.getEncoder().encodeToString(encryptedBytes));
            }
            writer.flush(); // Vider le flux de sortie
        } // Les flux d'entrée et de sortie seront fermés automatiquement ici
        System.out.println("Attente");
        while (!Files.isWritable(outputFile.toPath())) {
            System.out.println("uwu");
            Thread.sleep(1000); // Attendre une seconde avant de vérifier à nouveau
        }

        return outputFile;
    }*/

    public static File encrypt(File inputFile, String outputPath, byte[] key) throws Exception {
        // Création de l'objet Cipher pour le chiffrement
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));

        // Ouverture des fichiers d'entrée et de sortie
        FileInputStream inputStream = new FileInputStream(inputFile);
        File encryptedFile = new File(outputPath);
        FileOutputStream outputStream = new FileOutputStream(encryptedFile);

        // Chiffrement du fichier
        byte[] inputBytes = new byte[(int) inputFile.length()];
        inputStream.read(inputBytes);
        byte[] outputBytes = cipher.doFinal(inputBytes);
        outputStream.write(outputBytes);

        // Fermeture des flux de lecture et d'écriture
        inputStream.close();
        outputStream.close();

        // Retourne le fichier chiffré
        return encryptedFile;
    }




    public static File decrypt(File encryptedFile, File outputFile, byte[] key) throws Exception {
        String outputPath = outputFile.getPath();
        // Création de l'objet Cipher pour le déchiffrement
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));

        // Ouverture des fichiers d'entrée et de sortie
        FileInputStream inputStream = new FileInputStream(encryptedFile);
        File decryptedFile = new File(outputPath);
        FileOutputStream outputStream = new FileOutputStream(decryptedFile);

        // Déchiffrement du fichier
        byte[] inputBytes = new byte[(int) encryptedFile.length()];
        inputStream.read(inputBytes);
        byte[] outputBytes = cipher.doFinal(inputBytes);
        outputStream.write(outputBytes);

        // Fermeture des flux de lecture et d'écriture
        inputStream.close();
        outputStream.close();

        // Retourne le fichier déchiffré
        return decryptedFile;
    }
}
