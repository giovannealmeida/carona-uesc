package br.com.versalius.carona.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class Ride implements Serializable {

    //    Ride status
    public static final int RIDE_OPEN = 0;
    public static final int RIDE_CLOSED = 1;
    public static final int RIDE_FULL = 2;

    private int id;
    private User driver;
    private List<User> passengers;
    private int status;
    private int availableSits; /* driver.getVehicle().getNumSits() - passengers.size() */
    private String origin;
    private String destinationCity;
    private String destinationNeighborhood;
    private Calendar departTime;

    public Ride(int id, User driver, List<User> passengers, int status, int availableSits, String origin, String destinationCity, String destinationNeighborhood, Calendar departTime) {
        this.id = id;
        this.driver = driver;
        this.passengers = passengers;
        this.status = status;
        this.availableSits = availableSits;
        this.origin = origin;
        this.destinationCity = destinationCity;
        this.destinationNeighborhood = destinationNeighborhood;
        this.departTime = departTime;
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
        int numPassengers = getPassengers() == null?0:getPassengers().size();
        return getDriver().getActiveCar().getNumSits() - numPassengers;
    }

    public String getOrigin() {
        return origin;
    }

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
//        return getDepartTime().toString();
        return "18:00";
    }

    public String getFullDestination() {
        return getDestinationNeighborhood() + " - " + getDestinationCity();
    }
}
