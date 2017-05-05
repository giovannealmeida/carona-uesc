package br.com.versalius.carona.interfaces;

import br.com.versalius.carona.models.User;
import br.com.versalius.carona.models.Vehicle;

/**
 * Created by Giovanne on 04/05/2017.
 */

public interface UserUpdateListener {
    /**
     * Devolve o usuário com as informações atualizadas. Deve ter suas informações atualizadas no
     * header do Drawer
     *
     * @param user Usuário com as informações atualizadas
     */
    void OnUserPreferencesUpdate(User user);

    /**
     * Devolve o veículo escolhido pelo usuário para ser o novo veículo padrão. Deve ter suas
     * informações atualizadas no header do Drawer
     *
     * @param vehicle Veículo principal com informações atualizadas
     */
    void OnVehicleUpdate(Vehicle vehicle);
}
