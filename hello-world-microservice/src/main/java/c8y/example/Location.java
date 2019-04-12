package c8y.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.minidev.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    
    private String city;
    private String country_code;
    private String latitude;
    private String longitude;
    private String ip;

    public JSONObject toJSON () {
        JSONObject location     = new JSONObject();
        JSONObject c8y_Position = new JSONObject();

        c8y_Position.put("lat", this.latitude);
        c8y_Position.put("lng", this.longitude);

        location.put("c8y_Position", c8y_Position);
        location.put("type", "c8y_LocationUpdate");
        location.put("text", "Accessed from " + ip + 
                             " (" + (this.city != null ? this.city + ", " : "") + this.country_code + ")");

        return location;
    }

    @Override
    public String toString() {
        return this.toJSON().toJSONString();
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the country_code
     */
    public String getCountry_code() {
        return country_code;
    }

    /**
     * @param country_code the country_code to set
     */
    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

}
