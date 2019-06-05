package c8y.example;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Credentials {
    public static String USERNAME;
    public static String PASSWD;
    
    public static String IPSTACK_KEY;

    public static void loadCredentials () throws IOException {
        var props = new Properties();
        var credentials = new FileReader("/etc/my-first-microservice/credentials.properties");

        props.load(credentials);
        
        USERNAME = props.getProperty("c8y.tenant.id") + "/" + props.getProperty("c8y.user");
        PASSWD   = props.getProperty("c8y.password");
        
        IPSTACK_KEY = props.getProperty("ipstack.key");
    }
}
