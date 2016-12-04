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
    public static final int VEHICLE_TYPE_CAR = 0;
    public static final int VEHICLE_TYPE_MOTO = 1;

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

    public Vehicle(JSONObject json) {
        try {
            this.id = json.getInt("id");
            this.type = json.getInt("type");
            this.brand = json.getString("brand");
            this.model = json.getString("model");
            this.air = json.getBoolean("air");
            this.numDoors = json.getInt("num_doors");
            this.numSits = json.getInt("num_sits");
            this.plate = json.getString("plate");
            this.colorName = json.getString("colorName_name");
            this.colorCode = json.getString("colorName_code");
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
}
