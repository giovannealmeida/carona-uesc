package br.com.versalius.carona.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

import br.com.versalius.carona.R;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;
import br.com.versalius.carona.utils.DBHelper;
import br.com.versalius.carona.utils.ProgressDialogHelper;
import br.com.versalius.carona.utils.SessionHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class VehicleSettingsActivity extends AppCompatActivity implements View.OnFocusChangeListener, TextWatcher, ColorPickerDialogFragment.ColorPickerDialogListener, CompoundButton.OnCheckedChangeListener {

    private TextInputLayout tilBrand;
    private TextInputLayout tilModel;
    private TextInputLayout tilPlate;
    private TextInputLayout tilColorName;

    private EditText etBrand;
    private EditText etModel;
    private EditText etPlate;
    private EditText etColorName;

    private SwitchCompat swAirConditioner;
    private Spinner spNumDoors;
    private Spinner spNumSits;

    private MultiStateToggleButton swVehicleType;
    private CoordinatorLayout coordinatorLayout;

    private HashMap<String, String> formData;

    private MaterialDialog mMaterialDialog;
    private CircleImageView ivVehicleMainPic;
    private SimpleDraweeView ivUrlVehicle;

//    private GridView gvGallery;

    private static final int ACTION_RESULT_GET_IMAGE = 1000;
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private View colorPicker;
    //Inicializa com cor preta opaca
    private int selectedColor = Color.argb(255, Color.red(0), Color.green(0), Color.blue(0));
    private AppCompatButton btAdd;
    private AppCompatButton btSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_settings);
        EventBus.getDefault().register(this);
        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_add_vehicle);
        setUpViews();
        Vehicle vehicle = (Vehicle) getIntent().getSerializableExtra("vehicle");
        if (vehicle != null) { //Preenche tudo com os dados deste veículo para edição
            setData(vehicle);
        } else {//Não tem veículo pra edição, mostra botão de adicionar veículo
            btAdd.setVisibility(View.VISIBLE);
            btSave.setVisibility(View.GONE);
        }
//        setUpGallery();
    }

    private void setData(Vehicle vehicle) {
        formData.put("vehicle_id", String.valueOf(vehicle.getId()));
        getSupportActionBar().setTitle(R.string.title_edit_vehicle);

        ivUrlVehicle.setImageURI(Uri.parse(vehicle.getMainPhotoUrl()));

        if (vehicle.getType() == Vehicle.VEHICLE_TYPE_CAR) { //Se for carro
            swVehicleType.setValue(0);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.arr_num_doors, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spNumDoors.setAdapter(adapter);
            spNumDoors.setSelection(adapter.getPosition(String.valueOf(vehicle.getNumDoors())));

            adapter = ArrayAdapter.createFromResource(this, R.array.arr_num_sits, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spNumSits.setAdapter(adapter);
            spNumSits.setSelection(adapter.getPosition(String.valueOf(vehicle.getNumSits())));

        } else { //Se for moto
            swVehicleType.setValue(1);

            //Seta 0 portas
            String doors[] = {"0"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(VehicleSettingsActivity.this, android.R.layout.simple_spinner_item, doors);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spNumDoors.setAdapter(adapter);
            spNumDoors.setSelection(0);
            spNumDoors.setEnabled(false);
            //Seta 1 assento
            spNumSits.setSelection(0);
            spNumSits.setEnabled(false);

            swAirConditioner.setEnabled(false);
        }
        etBrand.setText(vehicle.getBrand());
        etModel.setText(vehicle.getModel());
        etPlate.setText(vehicle.getPlate());
        selectedColor = Color.parseColor(vehicle.getColorHex());
        ((GradientDrawable) colorPicker.getBackground().mutate()).setColor(selectedColor);
        etColorName.setText(vehicle.getColorName());
        swAirConditioner.setChecked(vehicle.hasAir());
    }

//    private void setUpGallery() {
//        gvGallery = (GridView) findViewById(R.id.gvGallery);
//        gvGallery.setAdapter(new GalleryAdapter(this,null));
//    }

    private void updateColorPicker(int color) {
        ((GradientDrawable) colorPicker.getBackground().mutate()).setColor(color);
    }

    private void setUpViews() {
        ivVehicleMainPic = (CircleImageView) findViewById(R.id.ivVehicleMainPic);
        ivUrlVehicle = (SimpleDraweeView) findViewById(R.id.ivUrlVehicle);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        ivUrlVehicle.getHierarchy().setRoundingParams(roundingParams);

        /* Pegar imagem */
        ImageButton btGetImage = (ImageButton) findViewById(R.id.btGetImage);
        btGetImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(VehicleSettingsActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i, ACTION_RESULT_GET_IMAGE);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(VehicleSettingsActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        callDialog("O aplicativo precisa de permissão para acessar a galeria do dispositivo", new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
                    } else {
                        ActivityCompat.requestPermissions(VehicleSettingsActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
                    }
                }
            }
        });

        swAirConditioner = (SwitchCompat) findViewById(R.id.swAirConditioner);
        swAirConditioner.setOnCheckedChangeListener(this);
        spNumDoors = (Spinner) findViewById(R.id.spNumDoors);
        spNumSits = (Spinner) findViewById(R.id.spNumSits);

        tilBrand = (TextInputLayout) findViewById(R.id.tilBrand);
        tilModel = (TextInputLayout) findViewById(R.id.tilModel);
        tilPlate = (TextInputLayout) findViewById(R.id.tilPlate);
        tilColorName = (TextInputLayout) findViewById(R.id.tilColorName);

        /* Instanciando campos */
        etBrand = (EditText) findViewById(R.id.etBrand);
        etModel = (EditText) findViewById(R.id.etModel);
        etPlate = (EditText) findViewById(R.id.etPlate);
        etColorName = (EditText) findViewById(R.id.etColorName);

        /* Adicionando FocusListener*/
        etBrand.setOnFocusChangeListener(this);
        etModel.setOnFocusChangeListener(this);
        etPlate.setOnFocusChangeListener(this);
        etColorName.setOnFocusChangeListener(this);

        /* Adicionando máscara */
        etPlate.addTextChangedListener(this); //ABC-1234

        swVehicleType = (MultiStateToggleButton) this.findViewById(R.id.toggleVehicleType);
        swVehicleType.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                //0 - Carro
                //1- Moto
                if (value == 0) {
                    formData.put("type", String.valueOf(Vehicle.VEHICLE_TYPE_CAR));

                    ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(VehicleSettingsActivity.this, R.array.arr_num_doors, android.R.layout.simple_spinner_item);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spNumDoors.setAdapter(spinnerArrayAdapter);

                    spNumDoors.setEnabled(true);
                    spNumSits.setEnabled(true);
                    swAirConditioner.setEnabled(true);

                } else {
                    formData.put("type", String.valueOf(Vehicle.VEHICLE_TYPE_MOTO));

                    //Seta 0 no número de portas
                    String doors[] = {"0"};
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(VehicleSettingsActivity.this, android.R.layout.simple_spinner_item, doors);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    spNumDoors.setAdapter(spinnerArrayAdapter);

                    swAirConditioner.setEnabled(false);
                    swAirConditioner.setChecked(false);
                    spNumDoors.setSelection(0);
                    spNumDoors.setEnabled(false);
                    spNumSits.setSelection(0);
                    spNumSits.setEnabled(false);
                }

            }
        });
        swVehicleType.setValue(0);

        colorPicker = findViewById(R.id.colorPicker);
        updateColorPicker(selectedColor);

        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialogFragment f = ColorPickerDialogFragment
                        .newInstance(0, "Selecione uma cor", null, selectedColor, false);

                f.setStyle(DialogFragment.STYLE_NORMAL, 0);
                f.show(getFragmentManager(), "d");
            }
        });

        //Usado quando um veículo é adicionado
        btAdd = (AppCompatButton) findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(VehicleSettingsActivity.this);
                if (NetworkHelper.isOnline(VehicleSettingsActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.showSpinner(getString(R.string.progress_wait), getString(R.string.adding_vehicle), true, false);
                        NetworkHelper.getInstance(VehicleSettingsActivity.this).insertVehicle(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        Vehicle vehicle = new Vehicle(jsonObject.getJSONObject("data"));
                                        saveVehicle(vehicle);
                                        CustomSnackBar.make(coordinatorLayout, "Veículo adicionado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();

                                        //Devolve o veículo pra ChangeVehicleFragment e encerra
                                        Intent data = new Intent();
                                        data.putExtra("vehicle", vehicle);
                                        if (getParent() == null) {
                                            setResult(Activity.RESULT_OK, data);
                                        } else {
                                            getParent().setResult(Activity.RESULT_OK, data);
                                        }
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao adiconar veículo", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao adiconar veículo", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    }
                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }
            }
        });

        //Usado quando um veículo é editado
        btSave = (AppCompatButton) findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(VehicleSettingsActivity.this);
                if (NetworkHelper.isOnline(VehicleSettingsActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.showSpinner(getString(R.string.progress_wait), getString(R.string.adding_vehicle), true, false);
                        NetworkHelper.getInstance(VehicleSettingsActivity.this).updateVehicle(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        Vehicle vehicle = new Vehicle(jsonObject.getJSONObject("data"));
                                        saveVehicle(vehicle);
                                        CustomSnackBar.make(coordinatorLayout, "Veículo alterado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();

                                        //Devolve o veículo pra ChangeVehicleFragment e encerra
                                        Intent data = new Intent();
                                        data.putExtra("vehicle", vehicle);
                                        if (getParent() == null) {
                                            setResult(Activity.RESULT_OK, data);
                                        } else {
                                            getParent().setResult(Activity.RESULT_OK, data);
                                        }
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao alterar veículo", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao alterar veículo", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    }
                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }
            }
        });
    }

    private void saveVehicle(Vehicle vehicle) {

        DBHelper helper = DBHelper.getInstance(this);
        if (vehicle != null) {
            ContentValues values = new ContentValues();
            values.put("id", vehicle.getId());
            values.put("is_default", vehicle.isDefault() ? 1 : 0);
            values.put("type", vehicle.getType());
            values.put("model", vehicle.getModel());
            values.put("brand", vehicle.getBrand());
            values.put("air", vehicle.hasAir());
            values.put("num_doors", vehicle.getNumDoors());
            values.put("num_sits", vehicle.getNumSits());
            values.put("plate", vehicle.getPlate());
            values.put("color_name", vehicle.getColorName());
            values.put("color_hex", vehicle.getColorHex());
            values.put("main_pic_url", vehicle.getMainPhotoUrl());

            if (btAdd.getVisibility() == View.VISIBLE) {
                helper.getDatabase().insert(DBHelper.TBL_VEHICLE, null, values);
            } else {
                helper.getDatabase().update(DBHelper.TBL_VEHICLE, values, "id = ?", new String[]{"" + vehicle.getId()});
            }
            if (vehicle.getGallery() != null) {
                values = new ContentValues();
                for (String picUrl : vehicle.getGallery()) {
                    values.put("vehicle_id", vehicle.getId());
                    values.put("pic_url", picUrl);
                    if (btAdd.getVisibility() == View.VISIBLE) {
                        helper.getDatabase().insert(DBHelper.TBL_VEHICLE_GALLERY, null, values);
                    } else {
                        helper.getDatabase().update(DBHelper.TBL_VEHICLE_GALLERY, values, "vehicle_id = ?", new String[]{"" + vehicle.getId()});
                    }
                }
            }
        }

        helper.close();
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {

        boolean isFocusRequested = false;

        if (!hasValidField(tilBrand, etBrand)) {
            tilBrand.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("brand", etBrand.getText().toString());
        }

        if (!hasValidField(tilModel, etModel)) {
            tilModel.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("model", etModel.getText().toString());
        }


        if (!hasValidPlate()) {
            if (!isFocusRequested) {
                tilPlate.requestFocus();
                isFocusRequested = true;
            }
        } else {
            if (!TextUtils.isEmpty(etPlate.getText())) {
                formData.put("plate", etPlate.getText().toString());
            }
        }

        if (!hasValidField(tilColorName, etColorName)) {
            tilColorName.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("color_name", etColorName.getText().toString());
        }


        formData.put("air", swAirConditioner.isChecked() ? "true" : "false");
        formData.put("num_doors", (spNumDoors.getSelectedItem().equals("0") ? "false" : (String) spNumDoors.getSelectedItem()));
        formData.put("num_sits", (String) spNumSits.getSelectedItem());

        formData.put("user_id", new SessionHelper(this).getUserId());
        formData.put("color_hex", colorToHexString(selectedColor));

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    /**
     * Verifica se o campo é vazio e se for seta a mensagem de erro informando que é requerido
     *
     * @param textInputLayout
     * @param editText
     * @return
     */
    private boolean hasValidField(TextInputLayout textInputLayout, EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            textInputLayout.setError(getResources().getString(R.string.error_field_required));
            return false;
        }

        textInputLayout.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidPlate() {
        String plate = etPlate.getText().toString().trim();
        String plateSections[] = plate.split("-");

        //[A-Z]{3}-[0-9]{4}
        Pattern p = Pattern.compile("[A-Z]{3}[-]\\d{4}");

        if (!TextUtils.isEmpty(plate)) {
            if (((plate.length() < 8) || (plateSections.length < 2)) ||
                    ((plateSections[1].length() != 4) || (plateSections[0].length() != 3)) ||
                    !p.matcher(plate).matches()) {
                tilPlate.setError(getString(R.string.error_invalid_plate));
                return false;
            }
        } else {
            tilPlate.setError(getString(R.string.error_field_required));
            return false;
        }
        tilPlate.setErrorEnabled(false);
        return true;
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
                case R.id.etBrand:
                    hasValidField(tilBrand, etBrand);
                    break;
                case R.id.etModel:
                    hasValidField(tilModel, etModel);
                    break;
                case R.id.etPlate:
                    hasValidPlate();
                    break;
                case R.id.etColorName:
                    hasValidField(tilColorName, etColorName);
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
                        ActivityCompat.requestPermissions(VehicleSettingsActivity.this, permissions, REQUEST_PERMISSION_CODE);
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
        ivVehicleMainPic.setImageBitmap(bitmap);
        ivVehicleMainPic.setVisibility(View.VISIBLE);
        ivUrlVehicle.setVisibility(View.GONE);

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
        isErasing = (after < count);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        int digits = s.toString().length();
                /* Se não está apagando, verifica se algo precisa ser adicionado */
        if (!isErasing) {
            if (digits == 3) {
                s.append("-");
            } else if (digits == 4) {
                String lastChar = s.toString().substring(digits - 1);
                if (!lastChar.equals("-")) {
                    String initialString = s.toString().substring(0, digits - 1);
                    s.clear();
                    s.append(initialString + "-" + lastChar);
                }
            }
        } else { /* Se apagou o último dígito deixando o número no formato (99)9999-9999 */
//            if (digits == 13) {
//                try {
//                    String currentDigits[] = s.toString().split("-");
//                    if (currentDigits[1].length() == 3) {
//                        currentDigits[0] = new StringBuilder(currentDigits[0]).insert(currentDigits[0].length() - 1, "-").toString();
//                        s.clear();
//                        s.append(currentDigits[0] + currentDigits[1]);
//                    }
//                } catch (Exception e) {
//                    //TODO: Lançar exceção
//                }
//            }
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        //Força ser cor opaca
        selectedColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
        updateColorPicker(selectedColor);
    }

    private static String colorToHexString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.swAirConditioner:
                if (swAirConditioner.isChecked()) {
                    formData.put("air", "true");
                } else {
                    formData.put("air", "false");
                }
                break;
        }
    }
}
