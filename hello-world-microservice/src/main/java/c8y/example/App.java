package c8y.example;

import java.util.HashMap;
import java.util.Map;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@MicroserviceApplication
@RestController
public class App {
    public static void main (String[] args) {
        SpringApplication.run(App.class, args);
    }

    /**
     * TODO: Javadoc
     */
    @RequestMapping("hello")
    public String greeting (@RequestParam(value = "name", defaultValue = "World") String you) {
        return "Hello " + you + "!";
    }

    // TODO: @RequestMapping("/") redirect to hello

    /**
     * TODO: Javadoc
     */
    @RequestMapping("environment")
    public Map<String, String> environment () {
        Map<String, String> env = System.getenv();
        Map<String, String> map = new HashMap<>();

        map.put("Microservice", env.get("C8Y_BOOTSTRAP_USER").substring(17));
        map.put("URL", env.get("C8Y_BASEURL"));
        map.put("JDK Version", env.get("JAVA_VERSION"));
        map.put("Tenant", env.get("C8Y_BOOTSTRAP_TENANT"));
        map.put("Isolation", env.get("C8Y_MICROSERVICE_ISOLATION"));

        return map;
    }
}
