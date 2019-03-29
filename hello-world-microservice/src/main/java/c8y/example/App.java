package c8y.example;

import java.util.HashMap;
import java.util.Map;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import com.cumulocity.model.authentication.CumulocityCredentials;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.PlatformImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@MicroserviceApplication
@RestController
public class App {

    private static Map<String, String> C8Y_ENV = null;
    private static Platform platform;

    public static void main (String[] args) {
        SpringApplication.run(App.class, args);

        C8Y_ENV  = getEnvironmentValues();
        platform = new PlatformImpl("<URL>", new CumulocityCredentials("<user>", "<passwd>"));
    }

    /** 
     * Get the environment variables of the container 
     */
    private static Map<String, String> getEnvironmentValues () {
        Map<String, String> env = System.getenv();
        Map<String, String> map = new HashMap<>();

        map.put("app_name", env.get("APPLICATION_NAME"));
        map.put("type", "Microservice");
        map.put("URL", env.get("C8Y_BASEURL"));
        map.put("JDK", env.get("JAVA_VERSION"));
        map.put("tenant", env.get("C8Y_BOOTSTRAP_TENANT"));
        map.put("isolation", env.get("C8Y_MICROSERVICE_ISOLATION"));
        map.put("memory", env.get("MEMORY_LIMIT"));

        return map;
    }


    /* * * * * * * Application endpoints * * * * * * */

    // Greeting endpoints
    @RequestMapping("hello")
    public String greeting (@RequestParam(value = "name", defaultValue = "World") String you) {
        return "Hello " + you + "!";
    }

    @RequestMapping("/") 
    public String root () {
        return greeting("World");
    }

    // Return the environment variables of the container 
    @RequestMapping("environment")
    public Map<String, String> environment () {
        return C8Y_ENV;
    }

    @RequestMapping("subscriptions")
    public String subscriptions () {


        //C8Y_ENV.get("URL") + "/application/currentApplication/subscriptions" 

        return "subscriptions";
    }

}
