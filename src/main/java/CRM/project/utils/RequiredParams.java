package CRM.project.utils;

import java.io.IOException;

public class RequiredParams {

    public static String emailUrl;
    public static String authServClientId;
    public static String authservauthurl;
    public static String secretKey;
    public static String authServPassword;
    public static String authServUsername;
    public static String authServGrantType;
    public static String authServClientSecret;



    static {
        try {
            authServClientSecret = PropertyReader.readProperties("authServClientSecret");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        try {
            emailUrl = PropertyReader.readProperties("emailUrl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        try {
            authservauthurl = PropertyReader.readProperties("authservauthurl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        try {
            secretKey = PropertyReader.readProperties("secretKey");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            authServPassword = PropertyReader.readProperties("authServPassword");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    static {
        try {
            authServUsername = PropertyReader.readProperties("authServUsername");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    static {
        try {
            authServGrantType = PropertyReader.readProperties("authServGrantType");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            authServClientId = PropertyReader.readProperties("authServClientId");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




}
