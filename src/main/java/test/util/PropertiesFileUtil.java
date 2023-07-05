package test.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFileUtil {
    public static Properties readPropertiesFile(String filePath) {
        Properties prop = new Properties();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}
