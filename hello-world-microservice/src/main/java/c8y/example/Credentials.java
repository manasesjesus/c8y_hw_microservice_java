package c8y.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class Credentials {
    // Credentials
    public String USERNAME;
    public String PASSWD;
    public String URL;

    public Credentials () {
        try {
            Properties props   = new Properties();
            Reader credentials = new FileReader("resources/credentials.properties");

            props.load(credentials);
            USERNAME = props.getProperty("c8y.tenant.id") + "/" + props.getProperty("c8y.username");
            PASSWD = props.getProperty("c8y.passwd");
            URL    = props.getProperty("c8y.url");

        } catch (IOException e) {
            System.err.println("[ERROR] Unable to load the user credentials");
            System.exit(007); 
        }        
    }
}
