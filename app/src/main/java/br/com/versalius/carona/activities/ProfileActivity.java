package br.com.versalius.carona.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.versalius.carona.R;
import br.com.versalius.carona.models.Ride;
import br.com.versalius.carona.models.User;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadUserAndSetupUI(getIntent().getExtras().getString("id"));
    }

    private void loadUserAndSetupUI(String id) {
        if(id != null){
            findViewById(R.id.previewWarn).setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            NetworkHelper.getInstance(this).getRideById(id, new ResponseCallback() {
                @Override
                public void onSuccess(String jsonStringResponse) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                        if(jsonObject.getBoolean("status")){
                            Ride ride = new Ride(jsonObject.getJSONObject("ride"));
                            setupUI(ride);
                        } else {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this,"Falha ao exibir carona",Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(VolleyError error) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this,"Falha ao exibir carona",Toast.LENGTH_LONG).show();
                    finish();
                }
            });

        } else {
            loadLocalUser();
        }
    }

    /**
     * Carrega a view com dados do usuário logado
     */
    private void loadLocalUser() {
        /*
        Para o usuário chegar a carregar essa tela, ele deve ter pedido pra ver o perfil de alguém.
        TODO: verificar se existe uma carona pra esse usuário e exibir (??)
         */
    }

    /**
     * Carrega a view com dados da carona trazidas do banco
     * @param ride - Carona que vai preencher a tela
     */
    private void setupUI(Ride ride){

    }
}
