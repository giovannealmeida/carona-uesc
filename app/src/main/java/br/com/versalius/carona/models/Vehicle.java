package br.com.versalius.carona.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.versalius.carona.network.NetworkHelper;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class Vehicle implements Serializable {

//    Vehicle types
    public static final int VEHICLE_TYPE_CAR = 1;
    public static final int VEHICLE_TYPE_MOTO = 2;

    private long id;
    private int type;
    private String brand;
    private String model;
    private boolean air;
    private int numDoors;
    private int numSits;
    private String plate;
    private String colorName;
    private String colorHex; /*HEX code for the color*/
    private String mainPhotoUrl;
    private List<String> gallery;
    private boolean isDefault;

    public Vehicle(JSONObject json) {
        try {
            this.id = json.getInt("v_id");
            this.type = json.getInt("v_vehicle_type");
            this.brand = json.getString("v_brand");
            this.model = json.getString("v_model");
            if(json.getInt("v_air") == 1){
                this.air = true;
            }
            this.numDoors = json.getInt("v_num_doors");
            this.numSits = json.getInt("v_num_sit");
            this.plate = json.getString("v_plate");
            this.colorName = json.getString("v_color_name");
            this.colorHex = json.getString("v_color_hex");
            if(json.getInt("v_default") == 1){
                this.isDefault = true;
            }
            this.mainPhotoUrl =  NetworkHelper.DOMINIO + json.getString("v_main_photo_url");

            if(!json.isNull("v_gallery")) {
                this.gallery = new ArrayList<>();
                JSONArray gallery = json.getJSONArray("v_gallery");
                for (int i = 0; i < gallery.length(); i++) {
                    this.gallery.add(gallery.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Vehicle() {

    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public boolean hasAir() {
        return air;
    }

    public int getNumDoors() {
        return numDoors;
    }

    public int getNumSits() {
        return numSits;
    }

    public String getPlate() {
        return plate;
    }

    public String getColorName() {
        return colorName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getMainPhotoUrl() {
        return mainPhotoUrl;
    }

    public List<String> getGallery() {
        return gallery;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setAir(boolean air) {
        this.air = air;
    }

    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }

    public void setNumSits(int numSits) {
        this.numSits = numSits;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public void setMainPhotoUrl(String mainPhotoUrl) {
        this.mainPhotoUrl = mainPhotoUrl;
    }

    public void setGallery(List<String> gallery) {
        this.gallery = gallery;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
