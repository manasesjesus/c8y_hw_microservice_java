package c8y.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import com.cumulocity.model.authentication.CumulocityCredentials;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.PlatformImpl;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.event.EventFilter;

import net.minidev.json.JSONObject;

@MicroserviceApplication
@RestController
public class App {

    private Platform platform;
    private Map<String, String> C8Y_ENV = new HashMap<>();
    private final String trackerId = "<YOUR_TRACKER_ID>";
    private final String ipstackKey= "<YOUR_IPSTACK_KEY>";

	public static void main (String[] args) {
        SpringApplication.run(App.class, args);

        App microservice = new App();
        
        microservice.subsetEnvironmentValues();
        microservice.platformLogin();
        microservice.createAlarm();
    }
    

    /**
     * Get some of the environment variables of the container
     */
    private void subsetEnvironmentValues () {
        var env = System.getenv();

        C8Y_ENV.put("app_name", env.get("APPLICATION_NAME"));
        C8Y_ENV.put("url", env.get("C8Y_BASEURL"));
        C8Y_ENV.put("jdk", env.get("JAVA_VERSION"));
        C8Y_ENV.put("tenant", env.get("C8Y_TENANT"));
        C8Y_ENV.put("user", env.get("C8Y_USER"));
        C8Y_ENV.put("password", env.get("C8Y_PASSWORD"));
        C8Y_ENV.put("isolation", env.get("C8Y_MICROSERVICE_ISOLATION"));
        C8Y_ENV.put("memory_limit", env.get("MEMORY_LIMIT"));
    }
    
    
    /**
     * Login into the platform using the environment credentials
     */
    private void platformLogin () {
    	try {
    		// Platform credentials
            var username = C8Y_ENV.get("tenant") + "/" + C8Y_ENV.get("user");
            var password = C8Y_ENV.get("password");

            // Login to the platform
            platform = new PlatformImpl(C8Y_ENV.get("url"), new CumulocityCredentials(username, password));
        } 
    	catch (SDKException sdke) {
            if (sdke.getHttpStatus() == 401) {
                System.err.println("[ERROR] Security/Unauthorized. Invalid credentials!");
            }
        }
    }
    
    
    /**
     * Create a warning alarm if the current user has permissions
     */
    @SuppressWarnings("rawtypes")
	private void createAlarm () {
	    // Get current user from the platform
	    var currentUser = platform.getUserApi().getCurrentUser();
	
	    // Verify if the current user can create alarms
	    var canCreateAlarms = false;
	    for (Object role : currentUser.getEffectiveRoles()) {
	        if (((HashMap) role).get("id").equals("ROLE_ALARM_ADMIN")) {
	            canCreateAlarms = true;
	        }
	    } 
	
	    // Create a warning alarm
	    if (canCreateAlarms) {
	    	var source = new ManagedObjectRepresentation();
	        source.setId(GId.asGId(trackerId));
	    	
	    	var alarm = new AlarmRepresentation();
	    	alarm.setSeverity("WARNING");
	    	alarm.setSource(source);
	    	alarm.setType("c8y_Application__Microservice_started");
	    	alarm.setText("The microservice " + C8Y_ENV.get("app_name") + " has been started");
	    	alarm.setStatus("ACTIVE");
	    	alarm.setDateTime(new DateTime(System.currentTimeMillis()));
	    	
	        platform.getAlarmApi().create(alarm);
	    }
    }
    

    /**
     * Create a LocationUpdate event based on the client's IP 
     * 
     * @param String    The public IP of the client
     * @return The event
     */
    public EventRepresentation createLocationUpdateEvent (String ip) {

        // Get location details from ipstack
        var rest = new RestTemplate();
        var apiURL = "http://api.ipstack.com/" + ip + "?access_key=" + ipstackKey;
        var location = rest.getForObject(apiURL, Location.class);

        // Prepare a LocationUpdate event using Cumulocity's API
        var c8y_Position = new JSONObject();
        c8y_Position.put("lat", location.getLatitude());
        c8y_Position.put("lng", location.getLongitude());

        var source = new ManagedObjectRepresentation();
        source.setId(GId.asGId(trackerId));

        var event = new EventRepresentation();
        event.setSource(source);
        event.setType("c8y_LocationUpdate");
        event.setDateTime(new DateTime(System.currentTimeMillis()));
        event.setText("Accessed from " + ip + 
                      " (" + (location.getCity() != null ? location.getCity() + ", " : "") + location.getCountry_code() + ")");
        event.setProperty("c8y_Position", c8y_Position);
        event.setProperty("ip", ip);
        
        // Create the event in the platform
        platform.getEventApi().create(event);

        return event;
    }

    
    /* * * * * * * * * * Application endpoints * * * * * * * * * */

    // Check the microservice status/health (implemented by default)
    // GET /health

    // Greeting endpoints
    @RequestMapping("hello")
    public String greeting (@RequestParam(value = "name", defaultValue = "World") String you) {
        return "Hello " + you + "!";
    }

    @RequestMapping("/")
    public String root () {
        return greeting("World");
    }

    // Return the environment values
    @RequestMapping("environment")
    public Map<String, String> environment () {
        return C8Y_ENV;
    }

    // Track client's approximate location
    @RequestMapping("location/track")
    public String trackLocation (HttpServletRequest request) {
        // Get the public IP address and create the event
        return createLocationUpdateEvent(request.getHeader("x-real-ip")).toJSON();
    }

    // Get the tracked IPs and locations
    @RequestMapping("location/locations")
    public ArrayList<Object> getLocations (@RequestParam(value = "max", defaultValue = "5") int max) {
    	var locations = new ArrayList<Object>();
    	var filter = new EventFilter().byType("c8y_LocationUpdate");
        var eventCollection = platform.getEventApi().getEventsByFilter(filter).get(max);
    	
        eventCollection.getEvents().forEach((event) -> {
        	var map = new HashMap<String, Object>();
        	
        	map.put("ip", event.getProperty("ip"));
        	map.put("coordinates", event.getProperty("c8y_Position"));
        	map.put("when", event.getCreationDateTime().toString("yyyy-MM-dd hh:mm:ss"));
        	
        	locations.add(map);
        });
    	
        return locations;
    }
}
