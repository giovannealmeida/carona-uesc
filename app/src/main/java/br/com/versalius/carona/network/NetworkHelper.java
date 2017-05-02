package br.com.versalius.carona.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
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

//    private final String DOMINIO = "http://giog.000webhostapp.com/"; // Remoto
    public static final String DOMINIO = "http://10.1.1.108/caronauesc-web/"; // Repo
    private final String API = "api/";
    private final String LOGIN = API+"UserService/login";
    private final String SIGNUP = API+"UserService/signup";
    private final String SAVE_PREFS = API+"UserService/save_prefs";
    private final String CHECK_EMAIL = API+"UserService/email_check";
    private final String GET_RIDES = API+"RideService/get_by_status";
    private final String GET_USER = API+"UserService/get_user_by_id";

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

    /**
     * Salva as preferêncuas de conta
     * @param params - Dados a serem salvos
     * @param callback - Callback de resposta do servidor
     */
    public void savePreferences(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + SAVE_PREFS,
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
     * @param email   - Email e id do usuário
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

    public void getRidesByStatus(int status, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("status", Integer.toString(status));
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_RIDES,params),
                callback);
    }

    public void getUserById(String id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_USER,params),
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
