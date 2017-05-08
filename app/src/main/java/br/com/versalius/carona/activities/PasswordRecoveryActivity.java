package br.com.versalius.carona.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.versalius.carona.MainActivity;
import br.com.versalius.carona.R;
import br.com.versalius.carona.models.User;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.SessionHelper;

public class PasswordRecoveryActivity extends AppCompatActivity {

    private EditText mEmailView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvMessage = (TextView) findViewById(R.id.tvMessage);

        if(getIntent().getStringExtra("message")!= null) {
            tvMessage.setText(getIntent().getStringExtra("message"));
        }

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.etEmail);

        AppCompatButton btRecovery = (AppCompatButton) findViewById(R.id.btRecovery);
        btRecovery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tvMessage.setText("");
                sendRecoveryEmail();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void sendRecoveryEmail() {
        String email = mEmailView.getText().toString();

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            return;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            return;
        }

        toggleProgress(true);
        NetworkHelper.getInstance(this).recoverPassword(email, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    toggleProgress(false);
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if(jsonObject.getBoolean("status")){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(PasswordRecoveryActivity.this);
                        builder.setCancelable(false);
                        builder.setMessage(R.string.dialog_message_recovey_email_sent_confirmation)
                                .setNeutralButton(R.string.dialog_action_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                        builder.create().show();
                    } else {
                        tvMessage.setText(jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                toggleProgress(false);
                if(!NetworkHelper.isOnline(getApplicationContext())){
                    tvMessage.setText("Você está offline!");
                } else {
                    tvMessage.setText("Falha ao tentar se conectar com o servidor. Tente mais tarde.");
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void toggleProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}

