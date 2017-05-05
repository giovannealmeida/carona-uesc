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

    public static int GENDER_MALE = 1;
    public static int GENDER_FEMALE = 2;

    private int id;
    private List<Vehicle> vehicles;
    private String firstName;
    private String lastName;
    private int genderId;
    private String phone = "";
    private String whatsapp = "";
    private String city;
    private String neighborhood;
    private Calendar birthDate;
    private String email;
    private String password;
    private String photoUrl;

    //Preferences
    private boolean showEmail;
    private boolean showBirthday;
    private boolean showCity;
    private boolean showNeighborhood;
    private boolean showPhone;
    private boolean showWhatsapp;

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
            this.genderId = json.getInt("u_gender_id");
            if(!json.getString("u_phone").equals("null")) {
                this.phone = json.optString("u_phone", "");
            }

            if(!json.getString("u_whatsapp").equals("null")) {
                this.whatsapp = json.optString("u_whatsapp", "");
            }

            this.city = json.optString("u_city","");
            this.neighborhood = json.optString("u_neighborhood","");
            this.birthDate = Calendar.getInstance();
            this.birthDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(json.optString("u_birth_date","")));
            this.email = json.optString("email","");
            this.password = json.optString("password","");
            if(!json.isNull("u_pic_path")) {
                this.photoUrl = NetworkHelper.DOMINIO + json.getString("u_pic_path");
            }

            JSONObject prefs = json.getJSONObject("u_prefs");
            if(prefs.getInt("up_show_email") == 1) {
                this.showEmail = true;
            }
            if(prefs.getInt("up_show_birthday") == 1) {
                this.showBirthday = true;
            }
            if(prefs.getInt("up_show_city") == 1) {
                this.showCity = true;
            }
            if(prefs.getInt("up_show_neighborhood") == 1) {
                this.showNeighborhood = true;
            }
            if(prefs.getInt("up_show_phone") == 1) {
                this.showPhone = true;
            }
            if(prefs.getInt("up_show_whatsapp") == 1) {
                this.showWhatsapp = true;
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

    public String getPhone() {
        return phone;
    }

    public String getWhatsapp() {
        return whatsapp;
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

    public String getFormattedBirthday(String format){
        SimpleDateFormat s = new SimpleDateFormat(format);
        return s.format(getBirthDate().getTime());
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

    public boolean isShowEmail() {
        return showEmail;
    }

    public boolean isShowBirthday() {
        return showBirthday;
    }

    public boolean isShowCity() {
        return showCity;
    }

    public boolean isShowNeighborhood() {
        return showNeighborhood;
    }

    public boolean isShowPhone() {
        return showPhone;
    }

    public boolean isShowWhatsapp() {
        return showWhatsapp;
    }

    public int getGenderId() {
        return genderId;
    }
}
