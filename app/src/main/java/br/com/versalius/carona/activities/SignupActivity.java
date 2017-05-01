package br.com.versalius.carona.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import br.com.versalius.carona.R;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;
import br.com.versalius.carona.utils.ProgressDialogHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignupActivity extends AppCompatActivity implements View.OnFocusChangeListener, TextWatcher {

    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilPasswordAgain;
    private TextInputLayout tilCity;
    private TextInputLayout tilNeighborhood;
    private TextInputLayout tilPhone;
    private TextInputLayout tilWhatsapp;

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordAgain;
    private EditText etCity;
    private EditText etNeighborhood;
    private EditText etBirthday;
    private EditText etPhone;
    private EditText etWhatsapp;

    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;

    private CoordinatorLayout coordinatorLayout;

    private HashMap<String, String> formData;

    private MaterialDialog mMaterialDialog;
    private CircleImageView ivProfile;

    private static final int ACTION_RESULT_GET_IMAGE = 1000;
    private static final int REQUEST_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);
        EventBus.getDefault().register(this);
        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

//        getSupportActionBar().setLogo(R.drawable.toolbar_logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_signup);

        setUpViews();
    }

    private void setUpViews() {
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        /* Pegar imagem */
        ImageButton btGetImage = (ImageButton) findViewById(R.id.btGetImage);
        btGetImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SignupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i, ACTION_RESULT_GET_IMAGE);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(SignupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        callDialog("O AkiJob precisa de permissão para acessar os arquivos do dispositivo", new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
                    } else {
                        ActivityCompat.requestPermissions(SignupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
                    }
                }
            }
        });

        /* Instanciando layouts */
        tilFirstName = (TextInputLayout) findViewById(R.id.tilFirstName);
        tilLastName = (TextInputLayout) findViewById(R.id.tilLastName);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        tilPasswordAgain = (TextInputLayout) findViewById(R.id.tilPasswordAgain);
        tilCity = (TextInputLayout) findViewById(R.id.tilCity);
        tilNeighborhood = (TextInputLayout) findViewById(R.id.tilNeighborhood);
        tilPhone = (TextInputLayout) findViewById(R.id.tilPhone);
        tilWhatsapp = (TextInputLayout) findViewById(R.id.tilWhatsapp);


        /* Instanciando campos */
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPasswordAgain = (EditText) findViewById(R.id.etPasswordAgain);
        etCity = (EditText) findViewById(R.id.etCity);
        etNeighborhood = (EditText) findViewById(R.id.etNeighborhood);
        etBirthday = (EditText) findViewById(R.id.etBirthday);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etWhatsapp = (EditText) findViewById(R.id.etWhatsapp);

        /* Adicionando FocusListener*/
        etFirstName.setOnFocusChangeListener(this);
        etLastName.setOnFocusChangeListener(this);
        etEmail.setOnFocusChangeListener(this);
        etPassword.setOnFocusChangeListener(this);
        etPasswordAgain.setOnFocusChangeListener(this);
        etCity.setOnFocusChangeListener(this);
        etNeighborhood.setOnFocusChangeListener(this);
        etPhone.setOnFocusChangeListener(this);
        etWhatsapp.setOnFocusChangeListener(this);

        /* Adicionando máscara */
        etPhone.addTextChangedListener(this);
        etWhatsapp.addTextChangedListener(this);

        /* Radio buttons*/
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
        rbMale = (RadioButton) findViewById(R.id.rbMale);
        rbFemale = (RadioButton) findViewById(R.id.rbFemale);

        /**** Seta o comportamento do DatePicker ****/
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Calendar nowCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etBirthday.setText(dateFormatter.format(newDate.getTime()));
            }

        }, nowCalendar.get(Calendar.YEAR), nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        etBirthday.setInputType(InputType.TYPE_NULL);
        etBirthday.setText(dateFormatter.format(nowCalendar.getTime()));
        //Abre o Date Picker com click (só funciona se o campo tiver foco)
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        //Abre o Date Picker assim que o campo receber foco
        etBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePickerDialog.show();
            }
        });

        Button btSingUp = (Button) findViewById(R.id.btSignup);
        btSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(SignupActivity.this);
                if (NetworkHelper.isOnline(SignupActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.createProgressSpinner("Aguarde", "Realizando cadastro", true, false);
                        NetworkHelper.getInstance(SignupActivity.this).doSignUp(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if(jsonObject.getBoolean("status")){
                                        CustomSnackBar.make(coordinatorLayout, "Cadastro realizado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao realizar cadastro", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar cadastro", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    }
                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }
            }
        });
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {

        boolean isFocusRequested = false;
        /* Verifica os campos de nome */
        if (!hasValidName()) {
            tilFirstName.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("first_name", etFirstName.getText().toString());
        }

        if (!hasValidLastName()) {
            tilLastName.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("last_name", etLastName.getText().toString());
        }

        /* Verifica o campo de e-mail*/
        if (!hasValidEmail()) {
            if (!isFocusRequested) {
                tilEmail.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("email", etEmail.getText().toString());
        }

        /* Verifica o campo de senha*/
        if (!hasValidPassword()) {
            if (!isFocusRequested) {
                tilPassword.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Verifica o campo de senha repetida*/
        if (!hasValidRepeatedPassword()) {
            if (!isFocusRequested) {
                tilPasswordAgain.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("password", etPassword.getText().toString());
        }

        /* Verifica a data de nascimento*/
        if (!hasValidBirthDay()) {
            if (!isFocusRequested) {
                etBirthday.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("birth_date", etBirthday.getText().toString());
        }

        /* Verifica os campos de cidade e bairro*/
        if (!hasValidCity()) {
            if (!isFocusRequested) {
                tilCity.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("city", etCity.getText().toString());
        }
        if (!hasValidNeighborhood()) {
            if (!isFocusRequested) {
                tilNeighborhood.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("neighborhood", etNeighborhood.getText().toString());
        }

        /* Verifica se os radio buttons estão descelecionados*/
        if (!hasValidGender()) {
            if (!isFocusRequested) {
                rgGender.requestFocus();
                isFocusRequested = true;
            }
        } else {
            if (rbFemale.isChecked()) {
                formData.put("gender_id", "2");
            } else {
                formData.put("gender_id", "1");
            }
        }

        /* Verifica os campos de telefone e Whatsapp*/
        if (!hasValidPhone()) {
            if (!isFocusRequested) {
                tilPhone.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("phone", etPhone.getText().toString());
        }

        if (!hasValidWhatsapp()) {
            if (!isFocusRequested) {
                tilWhatsapp.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("whatsapp", etWhatsapp.getText().toString());
        }

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidPhone() {
        String phone = etPhone.getText().toString().trim();
        String phoneNumber[] = phone.split("-");

        //O telefone não é obrigatório. Se não estiver vazio, verifica se é válido
        if (!TextUtils.isEmpty(phone)) {
            if (((phone.length() < 13) || (phoneNumber.length < 2))||
                    ((phoneNumber[1].length() != 4) || ((phoneNumber[0].length() != 8) && (phoneNumber[0].length() != 9)))) {
                tilPhone.setError(getResources().getString(R.string.err_msg_invalid_phone));
                return false;
            }
        }
        tilPhone.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidWhatsapp() {
        String phone = etWhatsapp.getText().toString().trim();
        String phoneNumber[] = phone.split("-");

        //O Whatsapp não é obrigatório. Se não estiver vazio, verifica se é válido
        if (!TextUtils.isEmpty(phone)) {
            if (((phone.length() < 13) || (phoneNumber.length < 2))||
                    ((phoneNumber[1].length() != 4) || ((phoneNumber[0].length() != 8) && (phoneNumber[0].length() != 9)))) {
                tilWhatsapp.setError(getResources().getString(R.string.err_msg_invalid_whatsapp));
                return false;
            }
        }
        tilWhatsapp.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidGender() {
        if (!rbMale.isChecked() && !rbFemale.isChecked()) {
            (findViewById(R.id.tvRgErrMessage)).setVisibility(View.VISIBLE);
            return false;
        }
        (findViewById(R.id.tvRgErrMessage)).setVisibility(View.GONE);
        return true;
    }

    private boolean hasValidRepeatedPassword() {
        String passwordAgain = etPasswordAgain.getText().toString().trim();
        if (!etPassword.getText().toString().trim().equals(passwordAgain)) {
            tilPasswordAgain.setError(getResources().getString(R.string.err_msg_dont_match_password));
            return false;
        }
        tilPasswordAgain.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidPassword() {
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password) || (password.length() < 6) || (password.length() > 22)) {
            tilPassword.setError(getResources().getString(R.string.err_msg_short_password));
            return false;
        }
        if (!TextUtils.isEmpty(etPasswordAgain.getText().toString().trim())) {
            hasValidRepeatedPassword();
        }
        tilPassword.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getResources().getString(R.string.err_msg_empty_email));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getResources().getString(R.string.err_msg_invalid_email));
            return false;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbEmailCheck);
        findViewById(R.id.ivEmailCheck).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).emailExists(email, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                findViewById(R.id.ivEmailCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject json = new JSONObject(jsonStringResponse);
                    if (json.getBoolean("status")) { /* O email existe */
                        tilEmail.setError(getResources().getString(R.string.err_msg_existing_email));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(SignupActivity.this, R.drawable.ic_close_circle));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 239, 83, 80));
                    } else {
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(SignupActivity.this, R.drawable.ic_check_circle));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 0, 192, 96));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                tilEmail.setError(getResources().getString(R.string.err_msg_server_fail));
                findViewById(R.id.ivEmailCheck).setVisibility(View.VISIBLE);
            }
        });

        tilEmail.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidName() {
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            tilFirstName.setError(getResources().getString(R.string.err_msg_empty_name));
            return false;
        } else if (etFirstName.getText().toString().trim().length() < 2) {
            tilFirstName.setError(getResources().getString(R.string.err_msg_short_name));
            return false;
        }

        tilFirstName.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidLastName() {
        if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            tilLastName.setError(getResources().getString(R.string.err_msg_empty_last_name));
            return false;
        } else if (etLastName.getText().toString().trim().length() < 2) {
            tilLastName.setError(getResources().getString(R.string.err_msg_short_last_name));
            return false;
        }
        tilLastName.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidCity() {
        if (TextUtils.isEmpty(etCity.getText().toString().trim())) {
            tilCity.setError(getResources().getString(R.string.err_msg_empty_city));
            return false;
        } else if (etCity.getText().toString().trim().length() < 4) {
            tilCity.setError(getResources().getString(R.string.err_msg_short_city));
            return false;
        }
        tilCity.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidNeighborhood() {
        if (TextUtils.isEmpty(etNeighborhood.getText().toString().trim())) {
            tilNeighborhood.setError(getResources().getString(R.string.err_msg_empty_neighborhood));
            return false;
        } else if (etNeighborhood.getText().toString().trim().length() < 4) {
            tilNeighborhood.setError(getResources().getString(R.string.err_msg_short_neighborhood));
            return false;
        }
        tilNeighborhood.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidBirthDay(){
        return !TextUtils.isEmpty(etBirthday.getText().toString().trim());
    }

    /**
     * NÃO REMOVER DE NOVO!!!!
     * Basicamente seta a ação de fechar a activity ao selecionar a seta na toolbar
     *
     * @param menuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (view.getId()) {
                case R.id.etFirstName:
                    hasValidName();
                    break;
                case R.id.etLastName:
                    hasValidLastName();
                    break;
                case R.id.etEmail:
                    hasValidEmail();
                    break;
                case R.id.etPassword:
                    hasValidPassword();
                    break;
                case R.id.etPasswordAgain:
                    hasValidRepeatedPassword();
                    break;
                case R.id.etCity:
                    hasValidCity();
                    break;
                case R.id.etNeighborhood:
                    hasValidNeighborhood();
                    break;
                case R.id.etPhone:
                    hasValidPhone();
                    break;
                case R.id.etWhatsapp:
                    hasValidWhatsapp();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_RESULT_GET_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            //Com base na URI da imagem selecionada, prepara o acesso ao banco de dados interno pra pegar a imagem
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, columns, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(columns[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            //Passa o caminho da imagem pra activity que vai fazer o crop
            startActivity(new Intent(this, CropActivity.class).putExtra("imagePath", imagePath));
        }
    }

    private void callDialog(String message, final String[] permissions) {
        mMaterialDialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_permission)
                .content(message)
                .positiveText(R.string.dialog_permission_agree_button)
                .negativeText(R.string.dialog_permission_disagree_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ActivityCompat.requestPermissions(SignupActivity.this, permissions, REQUEST_PERMISSION_CODE);
                        mMaterialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mMaterialDialog.dismiss();
                    }
                })
                .show();
    }

    /* O EventBus é usado somente pra trazer a imagem cortada de volta pra cá */
    @Subscribe
    public void onEvent(Bitmap bitmap) {
        /* Preview da imagem */
        ivProfile.setImageBitmap(bitmap);

        /* Codifica a imagem pra envio */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        formData.put("image", Base64.encodeToString(imageBytes, Base64.DEFAULT));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    boolean isErasing = false;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* Se depois da mudança não serão acrescidos caracteres, está apagando */
        isErasing = (after == 0);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String lastChar = "";

        int digits = s.toString().length();
                /* Se não está apagando, verifica se algo precisa ser adicionado */
        if (!isErasing) {
            if (digits > 0) {
                lastChar = s.toString().substring(digits - 1);
            }
            switch (digits) {
                case 1:
                    String digit = s.toString();
                    s.clear();
                    s.append("(" + digit);
                    break;
                case 3:
                    s.append(")");
                    break;
                        /* Quando o ")" é apagado */
                case 4:
                    if (!lastChar.equals(")")) {
                        String currentDigits = s.toString().substring(0, digits - 1);
                        s.clear();
                        s.append(currentDigits + ")" + lastChar);
                    }
                    break;
                        /* Assumindo números no formatp (99)9999-9999*/
                case 8:
                    s.append("-");
                    break;
                        /* Quando o "-" é apagado */
                case 9:
                    if (!lastChar.equals("-")) {
                        String currentDigits = s.toString().substring(0, digits - 1);
                        s.clear();
                        s.append(currentDigits + "-" + lastChar);
                    }
                    break;
                        /* Assumindo números no formatp (99)99999-9999*/
                case 14:
                    try {
                        String currentDigits[] = s.toString().split("-");
                        if (currentDigits[0].length() == 8) {
                            currentDigits[1] = new StringBuilder(currentDigits[1]).insert(1, "-").toString();
                            s.clear();
                            s.append(currentDigits[0] + currentDigits[1]);
                        }
                    } catch (Exception e) {
                        //TODO: Lançar exceção
                    }
                    break;
            }
        } else { /* Se apagou o último dígito deixando o número no formato (99)9999-9999 */
            if (digits == 13) {
                try {
                    String currentDigits[] = s.toString().split("-");
                    if (currentDigits[1].length() == 3) {
                        currentDigits[0] = new StringBuilder(currentDigits[0]).insert(currentDigits[0].length() - 1, "-").toString();
                        s.clear();
                        s.append(currentDigits[0] + currentDigits[1]);
                    }
                } catch (Exception e) {
                    //TODO: Lançar exceção
                }
            }
        }
    }
}
