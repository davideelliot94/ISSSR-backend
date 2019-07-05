package com.isssr.ticketing_system.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileManager {

    /**
     * Converte una stringa in base64 nel relativo file rappresentato.
     *
     * @param base64 stringa in base 64 da decodificare
     * @param fileName nome del file decofidicato
     * @param relativePath path dove salvare il file decodificato
     */
    public static void convertStringToFile(String base64,String fileName,String relativePath) {
        try {
            String[] tokens = base64.split(",");
            String[] tokens_bis = tokens[0].split("/");
            String[] tokens_ter = tokens_bis[1].split(";");
            String format = tokens_ter[0];
            byte[] imageByteArray = decodeFile(tokens[1]);

            File file = new File(relativePath, fileName + "." + format);
            FileOutputStream fileOutFile = new FileOutputStream(file);
            fileOutFile.write(imageByteArray);
            fileOutFile.flush();
            fileOutFile.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Questa funzione codifica un file in una stringa in base64.
     *
     * @param path path del file da codificare
     * @return la stringa in base64 del file codificato.
     */
    public static String encodeFile(String path){

        File file = new File(path);
        Path filelocation = file.toPath();


        try {
            byte[] data = Files.readAllBytes(filelocation);
            return Base64.encodeBase64String(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Decodifica una stringa in base64 in un array di byte
     *
     * @param fileDataString stringa da decodificare
     * @return array di byte della stringa decodificata.
     */
    private static byte[] decodeFile(String fileDataString) {
        return Base64.decodeBase64(fileDataString.getBytes());
    }


}
