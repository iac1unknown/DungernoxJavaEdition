import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// Imports data from resource files
public class DataDictionary {
    public static Map<String, Map<String, Object>> objectData = new HashMap<String, Map<String, Object>>();
    public static Map<String, BufferedImage> imageData = new HashMap<String, BufferedImage>();
    public static Map<String, File> soundData = new HashMap<String, File>();

    public static void init() {
        // Load object dictionary
        String json = null;
        try {
            Scanner scanner = new Scanner(new File("resource/objectdata.json"), "UTF-8");
            json = scanner.useDelimiter("\\A").next();
            scanner.close();
            objectData = new flexjson.JSONDeserializer<Map<String, Map<String, Object>>>().deserialize(json);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }


        // Load images
        File imageFolder = new File("resource/img");
        File[] listOfImageFiles = imageFolder.listFiles();

        for (int i = 0; i < listOfImageFiles.length; i++) {
            if (listOfImageFiles[i].isFile()) {
                try {
                    String name = listOfImageFiles[i].getName();
                    imageData.put(name.replaceFirst("[.][^.]+$", ""), Main.loadImage(listOfImageFiles[i]));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        // Load sounds
        File soundFolder = new File("resource/sound");
        File[] listOfSoundFiles = soundFolder.listFiles();

        for (int i = 0; i < listOfSoundFiles.length; i++) {
            if (listOfSoundFiles[i].isFile()) {
                String name = listOfSoundFiles[i].getName();
                soundData.put(name.replaceFirst("[.][^.]+$", ""), listOfSoundFiles[i]);
            }
        }
    }

    public static <T> T getObjectData(String type, String property) {
        return (T) objectData.get(type).get(property);
    }

    public static <T> T getObjectData(String type, String property, T placeholder) {
        Object value = objectData.get(type).get(property);
        if (value == null) {
            return placeholder;
        }
        return (T) value;
    }

    public static BufferedImage getImage(String name) {
        return imageData.get(name);
    }

    public static File getSound(String name) {
        return soundData.get(name);
    }
}
