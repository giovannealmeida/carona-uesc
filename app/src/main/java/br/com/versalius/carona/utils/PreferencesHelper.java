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
    public static final String USER_ID = "user_id";
    public static final String USER_IMAGE_URL = "user_image_url";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_CITY = "user_city";
    public static final String USER_NEIGHBORHOOD = "user_neighborhood";
    public static final String USER_BIRTHDAY = "user_birthday";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_WHATSAPP = "user_whatsapp";

    public static final String PREF_SHOW_EMAIL = "show_email";
    public static final String PREF_SHOW_BIRTHDAY = "show_birthday";
    public static final String PREF_SHOW_CITY = "show_city";
    public static final String PREF_SHOW_NEIGHBORHOOD = "show_neighborhood";
    public static final String PREF_SHOW_PHONE = "show_phone";
    public static final String PREF_SHOW_WHATSAPP = "show_whatsapp";

    private static PreferencesHelper instance;
    private SharedPreferences sharedPreferences;

    private PreferencesHelper(Context context) {
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
