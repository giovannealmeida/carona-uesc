package br.com.versalius.carona;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.versalius.carona.activities.LoginActivity;
import br.com.versalius.carona.models.User;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.PreferencesHelper;
import br.com.versalius.carona.utils.SessionHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SessionHelper session = new SessionHelper(this);
        if(session.isLogged()){

            NetworkHelper.getInstance(this).getUserById(session.getUserId(), new ResponseCallback() {
                @Override
                public void onSuccess(String jsonStringResponse) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                        if(jsonObject.getBoolean("status")){
                            User user = new User(jsonObject.getJSONObject("data"));
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("user",user);

                            //Atualiza usuário
                            new SessionHelper(SplashActivity.this).updateUser(user);

                            startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtras(bundle));
                        } else {
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.putExtra("message",jsonObject.getString("message"));
                            startActivity(intent);
                        }
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(VolleyError error) {
                    //Força logout
                    session.logout();
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    if(NetworkHelper.isOnline(getApplicationContext())) {
                        intent.putExtra("message", "Não foi possível realizar login. Tente novamente mais tarde.");
                    } else {
                        intent.putExtra("message", "Você está offline!");
                    }
                    startActivity(intent);
                    finish();
                }
            });


        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
