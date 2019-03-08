package c8y.example;

import java.util.HashMap;
import java.util.Map;
// import java.util.concurrent.Callable;

import com.cumulocity.microservice.autoconfigure.MicroserviceApplication;
/*import com.cumulocity.microservice.context.ContextService;
import com.cumulocity.microservice.context.credentials.MicroserviceCredentials;
import com.cumulocity.microservice.context.inject.TenantScope;
import com.cumulocity.microservice.subscription.model.core.PlatformProperties;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.event.EventApi;
import com.cumulocity.sdk.client.event.PagedEventCollectionRepresentation;

import org.springframework.beans.factory.annotation.Autowired;*/
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@MicroserviceApplication
@RestController
public class App {

/*    @Autowired
    private PlatformProperties platformProperties;
    @Autowired
    private ContextService<MicroserviceCredentials> contextService;
    @Autowired
    private EventApi eventApi;

    @TenantScope
    public EventApi eventApi(Platform platform) throws SDKException {
        return platform.getEventApi();
    }  

    public PagedEventCollectionRepresentation get10Events() {
        return contextService.callWithinContext(
                (MicroserviceCredentials) platformProperties.getMicroserviceBoostrapUser()
                , new Callable<PagedEventCollectionRepresentation>(){
            public PagedEventCollectionRepresentation call(){
                return eventApi.getEvents().get(10);
            }
        });
    } */

    public static void main (String[] args) {
        SpringApplication.run(App.class, args);
    }

    /**
     * TODO: Javadoc
     */
    @RequestMapping("hello")
    public String greeting (@RequestParam(value = "name", defaultValue = "World") String you) {

//        System.out.println(get10Events());

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
