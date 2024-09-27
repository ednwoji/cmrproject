package CRM.project.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

    public static String readProperties(String property) throws IOException {

        try {
            FileReader reader = new FileReader(File.separator + "u01" + File.separator + "app" + File.separator + "oracle" + File.separator + "shared" + File.separator + "properties" + File.separator + "u360payments.properties");
            Properties p = new Properties();
            p.load(reader);
            String propertyValue = p.getProperty(property);

            return propertyValue;
        }

        catch(Exception e) {
            return null;
        }

    }
}
