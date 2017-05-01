package br.com.versalius.carona.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import br.com.versalius.carona.utils.CustomFile;

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
    private String colorCode; /*HEX code for the color*/
    private CustomFile mainPhoto;
    private List<CustomFile> galery;
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
            this.colorCode = json.getString("v_color_hex");
            if(json.getInt("v_default") == 1){
                this.isDefault = true;
            }
//            TODO: pegar as imagens
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Vehicle(long id, int type, String brand, String model, boolean air, int numDoors, int numSits, String plate, String colorName, String colorCode, CustomFile mainPhoto, List<CustomFile> galery) {
        this.id = id;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.air = air;
        this.numDoors = numDoors;
        this.numSits = numSits;
        this.plate = plate;
        this.colorName = colorName;
        this.colorCode = colorCode;
        this.mainPhoto = mainPhoto;
        this.galery = galery;
        this.isDefault = true;
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

    public boolean isAir() {
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

    public String getColorCode() {
        return colorCode;
    }

    public CustomFile getMainPhoto() {
        return mainPhoto;
    }

    public List<CustomFile> getGalery() {
        return galery;
    }

    public Boolean isDefault() {
        return isDefault;
    }
}
