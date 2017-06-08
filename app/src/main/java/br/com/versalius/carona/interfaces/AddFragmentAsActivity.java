package br.com.versalius.carona.interfaces;

import android.support.v4.app.Fragment;

/**
 * Created by Giovanne on 08/06/2017.
 */

public interface AddFragmentAsActivity {
    /**
     * Adiciona um fragmento "sobre" outro alterando o ícone do drawer para Up Home como no
     * app do Gmail
     *
     * @param fragment - Fragmento a ser adicionado
     * @param title - Título que deve aparecer na Toolbar
     */
    void onAddFragment(Fragment fragment, String title);
    /**
     * Remove um fragmento do topo alterando o ícone do drawer de volta para as 3 linhas caso
     * este fragmento seja o último na stack
     *
     * @param title - Título que deve aparecer na Toolbar após a remoção do fragmento
     */
//    void onRemoveFragment(String title);
}
