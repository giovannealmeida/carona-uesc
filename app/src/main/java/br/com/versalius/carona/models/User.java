package br.com.versalius.carona.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.versalius.carona.network.NetworkHelper;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class User implements Serializable{

    private int id;
    private List<Vehicle> vehicles;
    private String firstName;
    private String lastName;
    private String city;
    private String neighborhood;
    private Calendar birthDate;
    private String email;
    private String password;
    private String photoUrl;

    public  User (JSONObject json) {
        try {
            this.id = json.getInt("u_id");
            if(!json.isNull("u_vehicles")) {
                JSONArray vehicles = json.getJSONArray("u_vehicles");
                this.vehicles = new ArrayList<>();
                for (int i = 0; i < vehicles.length(); i++) {
                    this.vehicles.add(new Vehicle(vehicles.getJSONObject(i)));
                }
            }
            this.firstName = json.optString("u_first_name","User");
            this.lastName = json.optString("u_last_name","");
            this.city = json.optString("u_city","");
            this.neighborhood = json.optString("u_neighborhood","");
            this.birthDate = Calendar.getInstance();
            this.birthDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(json.optString("u_birth_date","")));
            this.email = json.optString("email","");
            this.password = json.optString("password","");
            if(!json.isNull("u_pic_path")) {
                this.photoUrl = NetworkHelper.DOMINIO + json.getString("u_pic_path");
            }
        } catch (JSONException e){
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Vehicle getActiveCar(){
        if(vehicles != null) {
            for (Vehicle vehicle : vehicles) {
                if (vehicle.isDefault()) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCity() {
        return city;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public Calendar getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
