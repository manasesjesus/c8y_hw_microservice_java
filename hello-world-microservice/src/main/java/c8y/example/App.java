package c8y.example;

import static c8y.example.Credentials.loadCredentials;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import com.cumulocity.model.authentication.CumulocityCredentials;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.user.CurrentUserRepresentation;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.PlatformImpl;
import com.cumulocity.sdk.client.SDKException;

import org.joda.time.DateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import net.minidev.json.JSONObject;

@MicroserviceApplication
@RestController
public class App {

    private static Map<String, String> C8Y_ENV = null;
    private static Platform platform;
    private static boolean canCreateAlarms = false;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);

        // Load environment values
        C8Y_ENV = getEnvironmentValues();

        try {
            // Load platform credentials
            loadCredentials();

            // Connect to the platform
            platform = new PlatformImpl(Credentials.URL,
                    new CumulocityCredentials(Credentials.USERNAME, Credentials.PASSWD));

            // Add current user to the environment values
            CurrentUserRepresentation currentUser = platform.getUserApi().getCurrentUser();
            C8Y_ENV.put("username", currentUser.getUserName());

            // Verify if the current user can create alarms
            canCreateAlarms = currentUser.getEffectiveRoles().toString().indexOf("ROLE_ALARM_ADMIN") != -1;
        } catch (IOException ioe) {
            System.err.println("[ERROR] Unable to load the user credentials!");
        } catch (SDKException sdke) {
            if (sdke.getHttpStatus() == 401) {
                System.err.println("[ERROR] Security/Unauthorized. Invalid credentials!");
            }
        }

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println();

    }

    /**
     * Get the environment variables of the container
     */
    private static Map<String, String> getEnvironmentValues () {
        Map<String, String> env = System.getenv();
        Map<String, String> map = new HashMap<>();

        map.put("app_name", env.get("APPLICATION_NAME"));
        map.put("type", "Microservice");
        map.put("url", env.get("C8Y_BASEURL"));
        map.put("jdk", env.get("JAVA_VERSION"));
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
    public String root() {
        return greeting("World");
    }

    // Return the environment values
    @RequestMapping("environment")
    public Map<String, String> environment() {
        return C8Y_ENV;
    }

    @RequestMapping("track/locations")
    public String processData(HttpServletRequest request) {
        // Get public IP address
        String ip = request.getHeader("x-real-ip");

        // Get location details from ipstack
        RestTemplate rest = new RestTemplate();
        Location location = rest.getForObject("http://api.ipstack.com/" + ip + "?access_key=" + Credentials.IPSTACK_KEY,
                Location.class);

        // Prepare a LocationUpdate event using the API
        JSONObject c8y_Position = new JSONObject();
        c8y_Position.put("lat", location.getLatitude());
        c8y_Position.put("lng", location.getLongitude());

        ManagedObjectRepresentation source = new ManagedObjectRepresentation();
        source.setId(GId.asGId("1400"));                    // The ID of "My Tracker"

        EventRepresentation event = new EventRepresentation();
        event.setSource(source);
        event.setProperty("c8y_Position", c8y_Position);
        event.setType("c8y_LocationUpdate");
        event.setDateTime(new DateTime(System.currentTimeMillis()));
        event.setText("Accessed from " + ip + " (" + (location.getCity() != null ? location.getCity() + ", " : "")
                + location.getCountry_code() + ")");
        
        // Create the event in the platform
        platform.getEventApi().create(event);
 
        return event.toJSON();
    }

    @RequestMapping("subscriptions")
    public String subscriptions () {

        // Create an alarm of the new subscribed tenant using the Alarm API
        //platform.getAlarmApi().create();

        //C8Y_ENV.get("URL") + "/application/currentApplication/subscriptions" 

        return "subscriptions";
    }

}
