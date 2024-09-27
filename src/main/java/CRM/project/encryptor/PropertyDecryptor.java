package CRM.project.encryptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Slf4j
//@Component
public class PropertyDecryptor implements EnvironmentPostProcessor {
//public class PropertyDecryptor {

    private static final String PREFIX = "ENC(";
    private static final String SUFFIX = ")";

    @Value("${secret_key}")
    private static String FIXED_KEY;

//    @Value("${secret_key}")
//    private static String SECRET_KEY; // replace with your own secret key
    private static final String ALGORITHM = "AES";
//    private static final SecretKeySpec SECRET_KEY_SPEC = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("Processing the properties");
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    Object propertyValue = propertySource.getProperty(propertyName);
                    if (propertyValue instanceof String) {
                        String stringValue = (String) propertyValue;
                        if (stringValue.startsWith(PREFIX) && stringValue.endsWith(SUFFIX)) {
                            String encodedValue = stringValue.substring(PREFIX.length(), stringValue.length() - SUFFIX.length());
                            try {
                                String decodedValue = decrypt(encodedValue, FIXED_KEY);
                                if (propertyName.equals("spring.datasource.username")) {
                                    System.setProperty("spring.datasource.username", decodedValue);
                                } else if (propertyName.equals("spring.datasource.password")) {
                                    System.setProperty("spring.datasource.password", decodedValue);
                                }
                            } catch (Exception e) {
                                throw new IllegalStateException("Failed to decrypt property " + propertyName, e);
                            }
                        }
                    }
                }
            }
        }
    }




    public static String decrypt(String encodedValue, String fixedKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(fixedKey), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decodedValue = Base64.getDecoder().decode(encodedValue);
        byte[] decryptedValue = cipher.doFinal(decodedValue);
        return new String(decryptedValue, StandardCharsets.UTF_8);
    }

    public static String encrypt(String data, String fixedKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(fixedKey), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static class EncodedPropertySource extends PropertySource<String> {

        public EncodedPropertySource(String name, String source) {
            super(name, source);
        }

        @Override
        public Object getProperty(String name) {
            return this.source;
        }
    }

}