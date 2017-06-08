package br.com.versalius.carona.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.versalius.carona.utils.EncryptHelper;

/**
 * Created by Giovanne on 28/06/2016.
 */
public class NetworkHelper {
    private static final String TAG = NetworkHelper.class.getSimpleName();

    private static NetworkHelper instance;
    private static Context context;
    private RequestQueue requestQueue;

//    public static final String DOMINIO = "http://carona-uesc.000webhostapp.com/"; // Remoto
    public static final String DOMINIO = "http://10.1.1.107/caronauesc-web/"; // Repo
    //public static final String DOMINIO = "http://10.66.7.16/caronauesc-web/"; // Repo UDO

    private final String API = "api/";
    /*UserService*/
    private final String LOGIN = API + "UserService/login";
    private final String SIGNUP = API + "UserService/signup";
    private final String RECOVER_PASSWORD = API + "UserService/forgot_password_send_hash";
    private final String UPDATE = API + "UserService/update";
    private final String CHECK_EMAIL = API + "UserService/email_check";
    private final String GET_USER = API + "UserService/get_user_by_id";
    /*RideService*/
    private final String GET_RIDES = API + "RideService/get_by_status";
    private final String GET_RIDE_BY_ID = API + "RideService/get_by_id";
    /*VehicleService*/
    private final String UPDATE_DEFAULT_VEHICLE = API + "VehicleService/update_default";
    private final String INSERT_VEHICLE = API + "VehicleService/insert";
    private final String REMOVE_VEHICLE = API + "VehicleService/remove";
    private final String UPDATE_VEHICLE = API + "VehicleService/update";

    private NetworkHelper(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Pegar o contexto da aplicação garante que a requestQueue vai ser singleton e só
            // morre quando a aplicação parar
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    //Retorna uma instância estática de NetworkHelper
    public static synchronized NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    public void doLogin(String email, String password, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", EncryptHelper.SHA1(password));
        execute(Request.Method.POST, params, TAG, DOMINIO + LOGIN, callback);
    }

    public void doSignUp(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + SIGNUP,
                callback);
    }

    public void recoverPassword(String email, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + RECOVER_PASSWORD,
                callback);
    }

    /**
     * Salva as preferêncuas de conta
     *
     * @param params   - Dados a serem salvos
     * @param callback - Callback de resposta do servidor
     */
    public void savePreferences(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + UPDATE,
                callback);
    }

    /**
     * Verifica se o e-mail já existe.
     * <p>
     * TODO: Verificar o funcionamento desse controller (???)
     * Testes realizados com os parâmetros (email e id existem no banco e estão relacionados):
     * email_check?email=aphodyty_7@hotmail.com&user_id=108
     * <p>
     * Se somente um email é passado, dá erro.
     * Se um email e um id de usuário que existem no banco são passados, retorna 'false'
     * Se um email que não existe no banco e um id de usuário que existe são passados, retorna 'false'
     * Se um email que existe no banco e um id de usuário que não existe são passados, retorna 'true'
     *
     * @param email    - Email e id do usuário
     * @param callback
     */
    public void emailExists(String email, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);

        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + CHECK_EMAIL, params),
                callback);
    }

    public void getRidesByStatus(int status, int limit, int offset, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("status", Integer.toString(status));
        if (limit > 0) {
            params.put("limit", Integer.toString(limit));
            if (offset > 0) {
                params.put("offset", Integer.toString(offset));
            }
        }
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_RIDES, params),
                callback);
    }

    public void getRideById(String id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);

        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_RIDE_BY_ID, params),
                callback);
    }

    public void getUserById(String id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_USER, params),
                callback);
    }

    public void updateDefaultVehicle(String lastDefaultId, String newDefaultId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("last_default_id", lastDefaultId);
        params.put("new_default_id", newDefaultId);
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + UPDATE_DEFAULT_VEHICLE,
                callback);
    }

    public void insertVehicle(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + INSERT_VEHICLE,
                callback);
    }

    public void removeVehicle(String id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + REMOVE_VEHICLE,
                callback);
    }

    public void updateVehicle(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + UPDATE_VEHICLE,
                callback);
    }

    private void execute(int method, final HashMap params, String tag, String url, final ResponseCallback callback) {
        final CustomRequest request = new CustomRequest(
                method,
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("onResponse - LOG", "response: " + response);
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("onResponse - LOG", "response: " + error.getMessage());
                        if (callback != null) {
                            callback.onFail(error);
                        }
                    }
                });

//        request.setRetryPolicy(new DefaultRetryPolicy(
//                30000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(tag);
        getRequestQueue().add(request);

    }

    private String buildGetURL(String url, HashMap<String, String> params) {
        url += "?";
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            url += pair.getKey() + "=" + pair.getValue();
            it.remove(); // avoids a ConcurrentModificationException
            if (it.hasNext()) {
                url += "&";
            }
        }
        return url;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
