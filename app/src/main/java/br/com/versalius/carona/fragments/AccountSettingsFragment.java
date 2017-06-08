package br.com.versalius.carona.fragments;


import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import br.com.versalius.carona.R;
import br.com.versalius.carona.activities.CropActivity;
import br.com.versalius.carona.interfaces.AddFragmentAsActivity;
import br.com.versalius.carona.interfaces.MessageDeliveredListener;
import br.com.versalius.carona.interfaces.UserUpdateListener;
import br.com.versalius.carona.models.User;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;
import br.com.versalius.carona.utils.PreferencesHelper;
import br.com.versalius.carona.utils.ProgressDialogHelper;
import br.com.versalius.carona.utils.SessionHelper;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class AccountSettingsFragment extends Fragment implements View.OnFocusChangeListener, TextWatcher, CompoundButton.OnCheckedChangeListener {

    //Listeners
    private MessageDeliveredListener messageDeliveredListener;
    private UserUpdateListener userUpdateListener;

    //Campos
    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilCity;
    private TextInputLayout tilNeighborhood;
    private TextInputLayout tilPhone;
    private TextInputLayout tilWhatsapp;

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etCity;
    private EditText etNeighborhood;
    private EditText etBirthday;
    private EditText etPhone;
    private EditText etWhatsapp;

    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private TextView tvRgErrMessage;

    private SwitchCompat swShowBirthday;
    private SwitchCompat swShowEmail;
    private SwitchCompat swShowCity;
    private SwitchCompat swShowNeighborhood;
    private SwitchCompat swShowPhone;
    private SwitchCompat swShowWhatsapp;

    private HashMap<String, String> formData;

    private MaterialDialog mMaterialDialog;
    private CircleImageView ivProfile;
    private SimpleDraweeView ivUrlProfile;

    private static final int ACTION_RESULT_GET_IMAGE = 1000;
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private ImageButton btGetImage;
    private ImageButton btRemoveImage;

    public static AccountSettingsFragment newInstance() {
        return new AccountSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        formData = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_settings, container, false);

        setUpViews(rootView);

        return rootView;
    }

    private void setUpViews(View rootView) {
        final PreferencesHelper pref = PreferencesHelper.getInstance(getActivity());

        formData.put("user_id", pref.load(PreferencesHelper.USER_ID));

        AppCompatButton btShowProfile = (AppCompatButton) rootView.findViewById(R.id.btShowProfile);
        btShowProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RideFragment fragment = RideFragment.newInstance();
                ((AddFragmentAsActivity)getActivity()).onAddFragment(fragment,new SessionHelper(getActivity()).getUserFullName() + " (você)");
            }
        });

        /* Pegar imagem */
        btGetImage = (ImageButton) rootView.findViewById(R.id.btGetImage);
        btGetImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i, ACTION_RESULT_GET_IMAGE);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        callDialog("O AkiJob precisa de permissão para acessar os arquivos do dispositivo", new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
                    }
                }
            }
        });

        btRemoveImage = (ImageButton) rootView.findViewById(R.id.btRemoveImage);
        btRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ivUrlProfile.getVisibility() == View.VISIBLE) {
                    ivUrlProfile.setVisibility(View.GONE);
                    ivUrlProfile.setImageURI("");
                }
                ivProfile.setVisibility(View.VISIBLE);
                ivProfile.setImageBitmap(BitmapFactory.decodeResource(AccountSettingsFragment.this.getResources(),
                        R.drawable.ic_profile_placeholder));
                btRemoveImage.setVisibility(View.GONE);
                btGetImage.setVisibility(View.VISIBLE);

                formData.put("image", " ");
            }
        });

        ivProfile = (CircleImageView) rootView.findViewById(R.id.ivProfile);
        ivUrlProfile = (SimpleDraweeView) rootView.findViewById(R.id.ivUrlProfile);
//        Uri uri = Uri.parse(pref.load(PreferencesHelper.USER_IMAGE_URL));
//        if (!uri.toString().isEmpty() && !uri.toString().equals("null")) {
        if (!pref.load(PreferencesHelper.USER_IMAGE_URL).equals(NetworkHelper.DOMINIO+"null")) {
            ivUrlProfile.setImageURI(Uri.parse(pref.load(PreferencesHelper.USER_IMAGE_URL)));
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            ivUrlProfile.getHierarchy().setRoundingParams(roundingParams);
            btRemoveImage.setVisibility(View.VISIBLE);
            btGetImage.setVisibility(View.GONE);
        } else {
            ivUrlProfile.setVisibility(View.GONE);
            ivProfile.setVisibility(View.VISIBLE);
            btRemoveImage.setVisibility(View.GONE);
            btGetImage.setVisibility(View.VISIBLE);
        }

        tvRgErrMessage = (TextView) rootView.findViewById(R.id.tvRgErrMessage);
        /* Instanciando layouts */
        tilFirstName = (TextInputLayout) rootView.findViewById(R.id.tilFirstName);
        tilLastName = (TextInputLayout) rootView.findViewById(R.id.tilLastName);
        tilCity = (TextInputLayout) rootView.findViewById(R.id.tilCity);
        tilNeighborhood = (TextInputLayout) rootView.findViewById(R.id.tilNeighborhood);
        tilPhone = (TextInputLayout) rootView.findViewById(R.id.tilPhone);
        tilWhatsapp = (TextInputLayout) rootView.findViewById(R.id.tilWhatsapp);

        /* Instanciando campos */
        etFirstName = (EditText) rootView.findViewById(R.id.etFirstName);
        etFirstName.setText(pref.load(PreferencesHelper.USER_FIRST_NAME));
        etLastName = (EditText) rootView.findViewById(R.id.etLastName);
        etLastName.setText(pref.load(PreferencesHelper.USER_LAST_NAME));
        etEmail = (EditText) rootView.findViewById(R.id.etEmail);
        etEmail.setText(pref.load(PreferencesHelper.USER_EMAIL));
        etCity = (EditText) rootView.findViewById(R.id.etCity);
        etCity.setText(pref.load(PreferencesHelper.USER_CITY));
        etNeighborhood = (EditText) rootView.findViewById(R.id.etNeighborhood);
        etNeighborhood.setText(pref.load(PreferencesHelper.USER_NEIGHBORHOOD));
        etBirthday = (EditText) rootView.findViewById(R.id.etBirthday);
        etBirthday.setText(pref.load(PreferencesHelper.USER_BIRTHDAY));
        etPhone = (EditText) rootView.findViewById(R.id.etPhone);
        etPhone.setText(pref.load(PreferencesHelper.USER_PHONE));
        etWhatsapp = (EditText) rootView.findViewById(R.id.etWhatsapp);
        etWhatsapp.setText(pref.load(PreferencesHelper.USER_WHATSAPP));

        /* Instanciando switches */
        swShowBirthday = (SwitchCompat) rootView.findViewById(R.id.swShowBirthday);
        swShowBirthday.setOnCheckedChangeListener(this);
        if (Boolean.valueOf(pref.load(PreferencesHelper.PREF_SHOW_BIRTHDAY))) {
            swShowBirthday.setChecked(true);
            formData.put("show_birthday", "true");
        } else {
            formData.put("show_birthday", "false");
        }
        swShowEmail = (SwitchCompat) rootView.findViewById(R.id.swShowEmail);
        swShowEmail.setOnCheckedChangeListener(this);
        if (Boolean.valueOf(pref.load(PreferencesHelper.PREF_SHOW_EMAIL))) {
            swShowEmail.setChecked(true);
            formData.put("show_email", "true");
        } else {
            formData.put("show_email", "false");
        }
        swShowCity = (SwitchCompat) rootView.findViewById(R.id.swShowCity);
        swShowCity.setOnCheckedChangeListener(this);
        if (Boolean.valueOf(pref.load(PreferencesHelper.PREF_SHOW_CITY))) {
            swShowCity.setChecked(true);
            formData.put("show_city", "true");
        } else {
            formData.put("show_city", "false");
        }
        swShowNeighborhood = (SwitchCompat) rootView.findViewById(R.id.swShowNeighborhood);
        swShowNeighborhood.setOnCheckedChangeListener(this);
        if (Boolean.valueOf(pref.load(PreferencesHelper.PREF_SHOW_NEIGHBORHOOD))) {
            swShowNeighborhood.setChecked(true);
            formData.put("show_neighborhood", "true");
        } else {
            formData.put("show_neighborhood", "false");
        }
        swShowPhone = (SwitchCompat) rootView.findViewById(R.id.swShowPhone);
        swShowPhone.setOnCheckedChangeListener(this);
        if (Boolean.valueOf(pref.load(PreferencesHelper.PREF_SHOW_PHONE))) {
            swShowPhone.setChecked(true);
            formData.put("show_phone", "true");
        } else {
            formData.put("show_phone", "false");
        }
        swShowWhatsapp = (SwitchCompat) rootView.findViewById(R.id.swShowWhatsapp);
        swShowWhatsapp.setOnCheckedChangeListener(this);
        if (Boolean.valueOf(pref.load(PreferencesHelper.PREF_SHOW_WHATSAPP))) {
            swShowWhatsapp.setChecked(true);
            formData.put("show_whatsapp", "true");
        } else {
            formData.put("show_whatsapp", "false");
        }

        /* Adicionando FocusListener*/
        etFirstName.setOnFocusChangeListener(this);
        etLastName.setOnFocusChangeListener(this);
        etCity.setOnFocusChangeListener(this);
        etNeighborhood.setOnFocusChangeListener(this);
        etPhone.setOnFocusChangeListener(this);
        etWhatsapp.setOnFocusChangeListener(this);

        /* Adicionando máscara */
        etPhone.addTextChangedListener(this);
        etWhatsapp.addTextChangedListener(this);

        /* Radio buttons*/
        rgGender = (RadioGroup) rootView.findViewById(R.id.rgGender);
        rbMale = (RadioButton) rootView.findViewById(R.id.rbMale);
        rbFemale = (RadioButton) rootView.findViewById(R.id.rbFemale);

        if (Integer.valueOf(pref.load(PreferencesHelper.USER_GENDER_ID)) == 1) {
            rbMale.setChecked(true);
        } else {
            rbFemale.setChecked(true);
        }

        /**** Seta o comportamento do DatePicker ****/
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        Calendar birthDate = Calendar.getInstance();
        try {
            birthDate.setTime(dateFormatter.parse(pref.load(PreferencesHelper.USER_BIRTHDAY)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etBirthday.setText(dateFormatter.format(newDate.getTime()));
            }

        }, birthDate.get(Calendar.YEAR), birthDate.get(Calendar.MONTH), birthDate.get(Calendar.DAY_OF_MONTH));

        etBirthday.setInputType(InputType.TYPE_NULL);
        etBirthday.setText(pref.load(PreferencesHelper.USER_BIRTHDAY));
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

        Button btSave = (Button) rootView.findViewById(R.id.btAdd);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(getActivity());
                if (NetworkHelper.isOnline(getActivity())) {
                    if (isValidForm()) {
                        progressHelper.showSpinner("Aguarde", "Salvando", true, false);
                        NetworkHelper.getInstance(getActivity()).savePreferences(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        User user = new User(jsonObject.getJSONObject("data"));
                                        new SessionHelper(getActivity()).saveUser(user);
                                        userUpdateListener.OnUserPreferencesUpdate(user);
                                        messageDeliveredListener.onMessageDelivered("Atualização realizada com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS);
                                    } else {
                                        messageDeliveredListener.onMessageDelivered("Falha ao realizar atualização", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.ERROR);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                messageDeliveredListener.onMessageDelivered("Falha ao atualizar. Tente mais tarde!", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.ERROR);
                            }
                        });
                    }
                } else {
                    messageDeliveredListener.onMessageDelivered("Você está offline", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.ERROR);
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
                formData.put("gender_id", String.valueOf(User.GENDER_FEMALE));
            } else {
                formData.put("gender_id", String.valueOf(User.GENDER_MALE));
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
            if (((phone.length() < 13) || (phoneNumber.length < 2)) ||
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
            if (((phone.length() < 13) || (phoneNumber.length < 2)) ||
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
            tvRgErrMessage.setVisibility(View.VISIBLE);
            return false;
        }
        tvRgErrMessage.setVisibility(View.GONE);
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

    private boolean hasValidBirthDay() {
        return !TextUtils.isEmpty(etBirthday.getText().toString().trim());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_RESULT_GET_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            //Com base na URI da imagem selecionada, prepara o acesso ao banco de dados interno pra pegar a imagem
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, columns, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(columns[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            //Passa o caminho da imagem pra activity que vai fazer o crop
            startActivity(new Intent(getActivity(), CropActivity.class).putExtra("imagePath", imagePath));
        }
    }

    private void callDialog(String message, final String[] permissions) {
        mMaterialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_title_permission)
                .content(message)
                .positiveText(R.string.dialog_permission_agree_button)
                .negativeText(R.string.dialog_permission_disagree_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_PERMISSION_CODE);
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
        ivProfile.setVisibility(View.VISIBLE);
        ivUrlProfile.setVisibility(View.GONE);
        ivUrlProfile.setImageURI("");
        btRemoveImage.setVisibility(View.VISIBLE);

        /* Codifica a imagem pra envio */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        formData.put("image", Base64.encodeToString(imageBytes, Base64.DEFAULT));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MessageDeliveredListener) {
            messageDeliveredListener = (MessageDeliveredListener) context;
        }/* else {
            throw new RuntimeException(context.toString()
                    + " must implement MessageDeliveredListener");
        }*/
        if (context instanceof UserUpdateListener) {
            userUpdateListener = (UserUpdateListener) context;
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        messageDeliveredListener = null;
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.swShowBirthday:
                if (swShowBirthday.isChecked()) {
                    formData.put("show_birthday", "true");
                } else {
                    formData.put("show_birthday", "false");
                }
                break;
            case R.id.swShowEmail:
                if (swShowEmail.isChecked()) {
                    formData.put("show_email", "true");
                } else {
                    formData.put("show_email", "false");
                }
                break;
            case R.id.swShowCity:
                if (swShowCity.isChecked()) {
                    formData.put("show_city", "true");
                } else {
                    formData.put("show_city", "false");
                }
                break;
            case R.id.swShowNeighborhood:
                if (swShowNeighborhood.isChecked()) {
                    formData.put("show_neighborhood", "true");
                } else {
                    formData.put("show_neighborhood", "false");
                }
                break;
            case R.id.swShowPhone:
                if (swShowPhone.isChecked()) {
                    formData.put("show_phone", "true");
                } else {
                    formData.put("show_phone", "false");
                }
                break;
            case R.id.swShowWhatsapp:
                if (swShowWhatsapp.isChecked()) {
                    formData.put("show_whatsapp", "true");
                } else {
                    formData.put("show_whatsapp", "false");
                }
                break;
        }
    }
}
