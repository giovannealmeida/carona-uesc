package br.com.versalius.carona.interfaces;

/**
 * Created by Giovanne on 04/05/2017.
 */

public interface MessageDeliveredListener {
    void onMessageDelivered(String message, int duration, int type);
}
