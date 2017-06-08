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

public class Ride implements Serializable {

    //    Ride status
    //A carona está aberta e os passageiros podem solicitar entrada.
    public static final int RIDE_OPEN = 1;
    //A carona já foi fechada pelo motorista e está prestes a iniciar. Não é possível mais entrar nesta carona.
    public static final int RIDE_CLOSED = 2;
    //A carona está em andamento e os passageiros estão sendo transportados.
    public static final int RIDE_RUNNING = 3;
    //A carona terminou. Todos os passageiros já foram deixados em seus destinos.
    public static final int RIDE_FINISHED = 4;
    //O motorista cancelou a carona. Todos os passageiros foram removidos da carona.
    public static final int RIDE_CANCELED = 5;
    //A carona atingiu o número máximo de passageiros. Não há mais acento disponível.
    public static final int RIDE_FULL = 6;

    private int id;
    private User driver;
    private List<User> passengers;
    private int numPassengers;
    private int availableSits;
    private int status;
    private Origin origin;
    private String destinationCity;
    private String destinationNeighborhood;
    private Calendar departTime;

    public Ride(JSONObject json) {
        try {
            this.id = json.getInt("r_id");
            if (json.has("r_driver")) {
                this.driver = new User(json.getJSONObject("r_driver"));
            }
            //Ao mostrar o feed não se utiliza informações sobre passageiros...
            if (json.has("r_passengers") && !json.isNull("r_passengers")) {
                this.numPassengers = 0;
                JSONArray passengers = json.getJSONArray("r_passengers");
                this.passengers = new ArrayList<>();
                for (int i = 0; i < passengers.length(); i++) {
                    this.passengers.add(new User(passengers.getJSONObject(i)));
                    this.numPassengers++;
                }
            } else { //... apenas a quantidade
                this.numPassengers = json.getInt("num_passengers");
            }
            this.availableSits = json.getInt("r_available_sits_num");
            this.status = json.getInt("r_status_id");
            this.origin = new Origin(json.getJSONObject("r_origin"));
            this.destinationCity = json.getString("r_destination_city");
            this.destinationNeighborhood = json.getString("r_destination_neighborhood");
            this.departTime = Calendar.getInstance();
            this.departTime.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(json.optString("r_depart_time", "")));
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public User getDriver() {
        return driver;
    }

    public List<User> getPassengers() {
        return passengers;
    }

    public int getStatus() {
        return status;
    }

    public int getAvailableSits() {
//        int numPassengers = getPassengers() == null ? 0 : getPassengers().size();
//        return getDriver().getActiveCar().getNumSits() - numPassengers;
        return availableSits - numPassengers;
    }

    public String getOrigin() {
        return origin.getName();
    }

    public String getOriginComplement(){ return origin.getComplement();}

    public String getDestinationCity() {
        return destinationCity;
    }

    public String getDestinationNeighborhood() {
        return destinationNeighborhood;
    }

    public Calendar getDepartTime() {
        return departTime;
    }

    public String getDepartTimeString() {
        SimpleDateFormat s = new SimpleDateFormat("HH:mm");
        return s.format(getDepartTime().getTime());
    }

    public String getFullDestination() {
        return getDestinationNeighborhood() + " - " + getDestinationCity();
    }

    public void addPassenger(User passenger) {
        if (this.passengers == null)
            passengers = new ArrayList<>();

        this.passengers.add(passenger);
        this.numPassengers++;
    }

    public void removePassengerById(int passengerId) {
        if (passengers != null)
            for (User passenger : passengers)
                if (passenger.getId() == passengerId) {
                    passengers.remove(passenger);
                    numPassengers--;
                }
    }
}
