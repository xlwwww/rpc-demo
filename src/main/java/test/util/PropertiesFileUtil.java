package test.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
@Slf4j
public class PropertiesFileUtil {
    public static Properties readPropertiesFile(String filePath) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + filePath;
        }
        Properties prop = new Properties();
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            prop.load(inputStreamReader);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", filePath);
        }
        return prop;
    }
}
