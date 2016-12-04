package br.com.versalius.carona.models;

import java.io.Serializable;
import java.util.Calendar;

import br.com.versalius.carona.utils.CustomFile;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class User implements Serializable{

    private int id;
    private Vehicle vehicle;
    private String firstName;
    private String lastName;
    private String city;
    private String neighborhood;
    private Calendar birthDate;
    private String email;
    private String password;
//    private CustomFile photo;
    private int photo;

    public User(int id, Vehicle vehicle, String firstName, String lastName, String city, String neighborhood, Calendar birthDate, String email, String password, int photo) {
        this.id = id;
        this.vehicle = vehicle;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.neighborhood = neighborhood;
        this.birthDate = birthDate;
        this.email = email;
        this.password = password;
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
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

    public int getPhoto() {
        return photo;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }
}
