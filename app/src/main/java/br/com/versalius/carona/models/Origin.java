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

/**
 * Created by Giovanne on 03/12/2016.
 */

public class Origin implements Serializable {

    private int id;
    private String name;
    private String complement;

    public Origin(JSONObject json) {
        try {
            this.id = json.getInt("o_id");
            this.name = json.getString("o_name");
            this.complement = json.getString("o_complement");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public String getComplement() {
        return complement;
    }
}
