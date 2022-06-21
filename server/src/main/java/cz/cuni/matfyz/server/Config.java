package cz.cuni.matfyz.server;

import java.util.Properties;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class Config
{
    private static Properties properties;
    private static Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private Config() {}

    public static String get(String key)
    {
        if (properties == null)
            loadProperties();

        String property = properties.getProperty(key);
        if (property == null)
            LOGGER.error("Property '" + key + "' not found in configuration.");

        return property;
    }

    private static void loadProperties()
    {
        try
        {
            properties = new Properties();
            
            var url = ClassLoader.getSystemResource("application.properties");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            var configFile = new File(pathToFile);
            var reader = new FileReader(configFile);
    
            properties.load(reader);
        }
        catch (URISyntaxException exception)
        {
            LOGGER.error(exception.toString());
        }
        catch (FileNotFoundException exception)
        {
            LOGGER.error(exception.toString());
        }
        catch (IOException exception)
        {
            LOGGER.error(exception.toString());
        }
    }
}
