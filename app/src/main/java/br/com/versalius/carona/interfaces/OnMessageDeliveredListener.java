package br.com.versalius.carona.interfaces;

/**
 * Created by Giovanne on 04/05/2017.
 */

public interface OnMessageDeliveredListener {
    void showMessage(String message, int duration, int type);
}
