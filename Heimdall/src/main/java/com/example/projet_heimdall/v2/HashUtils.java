package com.example.projet_heimdall.v2;

//import standard de java
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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

    public static File encrypt(File inputFile, String outputPath, byte[] key) throws Exception {

        byte[] keyByte = Arrays.copyOfRange(key,0,16);
        // Création de l'objet Cipher pour le chiffrement
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyByte, "AES"));


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
        byte[] keyByte = Arrays.copyOfRange(key,0,16);
        String outputPath = outputFile.getPath();
        // Création de l'objet Cipher pour le déchiffrement
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyByte, "AES"));

        // Ouverture des fichiers d'entrée et de sortie
        FileInputStream inputStream = new FileInputStream(encryptedFile);
        File decryptedFile = new File(outputPath);
        FileOutputStream outputStream = new FileOutputStream(decryptedFile);

        // Déchiffrement du fichier
        try {
            byte[] inputBytes = new byte[(int) encryptedFile.length()];
            inputStream.read(inputBytes);
            byte[] outputBytes = cipher.doFinal(inputBytes);
            outputStream.write(outputBytes);
        } finally {
            // Fermeture des flux de lecture et d'écriture dans le bloc finally
            inputStream.close();
            outputStream.close();
        }

        // Retourne le fichier déchiffré
        return decryptedFile;
    }
}
