package br.com.versalius.carona.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class PreferencesHelper {
    // Nome dos arquivos XML
    public static final String USER_PREFERENCES = "br.com.versalius.carona";

    //Nome das chaves
    public static final String USER_FIRST_NAME = "user_first_name";
    public static final String USER_LAST_NAME = "user_last_name";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_ID = "user_id";

    private static PreferencesHelper instance;
    private SharedPreferences sharedPreferences;
    private Context context;

    private PreferencesHelper(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static PreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesHelper(context);
        }
        return instance;
    }

    /**
     * <p>Salva o objeto passado por parâmetro com a chave passada por parâmetro.</p>
     * <p>Se o objeto passado for um {@link HashMap}&lt;{@link String}, {@link String}&gt;, ele será iterado num
     * laço tendo cada um de seus pares inseridos ao {@link SharedPreferences} e o primeiro
     * parâmetro pode ser vazio ou nulo</p>
     */
    public void save(String key, Object obj) throws Exception {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            if (obj instanceof HashMap<?, ?>) {
                for (String k : (Set<String>) (((HashMap) obj).keySet())) {
                    String v = (String) ((HashMap) obj).get(k);
                    editor.putString(k, v);
                }
            } else {
                editor.putString(key, String.valueOf(obj));
            }
        } catch (Exception e) {
            throw new Exception(obj.getClass() + " não suportado para persistência no SharedPreferences.", e);
        }

        editor.commit();
    }

    public String load(String key) {
        return sharedPreferences.getString(key, "");
    }

    /**
     * Remove o valor referente à chave passada.
     *
     * @param key - Chave a qual o valor será removido.
     */
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().commit();
    }
}
