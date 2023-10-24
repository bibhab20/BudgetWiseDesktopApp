package util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class AppConfig {

    private Properties properties = new Properties();


    public AppConfig(String activeProfile) {
        String propertiesFile = activeProfile.equals("Default")? "/application.properties" : "/application-" + activeProfile + ".properties";
        log.info("property file used {}", propertiesFile);
        try (InputStream inputStream = AppConfig.class.getResourceAsStream(propertiesFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Properties getProperties() {
        return properties;
    }


}
