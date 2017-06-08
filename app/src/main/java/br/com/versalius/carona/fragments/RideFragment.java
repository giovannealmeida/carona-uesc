package br.com.versalius.carona.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.versalius.carona.R;
import br.com.versalius.carona.interfaces.MessageDeliveredListener;
import br.com.versalius.carona.models.Ride;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;

public class RideFragment extends Fragment {

    private SimpleDraweeView ivUrlProfile;
    private View vStarsCounter;
    private AppCompatButton btShowEvaluations;
    private AppCompatButton btGetRide;

    private TextView tvName;
    private TextView tvOrigin;
    private TextView tvComplement;
    private TextView tvDestination;
    private TextView tvVehicleType;
    private TextView tvVehicleBrandAndModel;
    private TextView tvAvailableSits;
    private TextView tvTime;
    private TextView tvEmail;
    private TextView tvWhatsapp;
    private TextView tvPhone;

    private String userId;

    public static RideFragment newInstance() {
        return new RideFragment();
    }

    @Override
    public void setArguments(Bundle b) {
        if (b.containsKey("user_id")) {
            userId = b.getString("user_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ride, container, false);

        ivUrlProfile = (SimpleDraweeView) rootView.findViewById(R.id.ivUrlProfile);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        ivUrlProfile.getHierarchy().setRoundingParams(roundingParams);

        vStarsCounter = rootView.findViewById(R.id.vStarsCounter);
        btShowEvaluations = (AppCompatButton) rootView.findViewById(R.id.btShowEvaluations);
        btGetRide = (AppCompatButton) rootView.findViewById(R.id.btGetRide);
        tvName = (TextView) rootView.findViewById(R.id.tvName);
        tvOrigin = (TextView) rootView.findViewById(R.id.tvOrigin);
        tvComplement = (TextView) rootView.findViewById(R.id.tvComplement);
        tvDestination = (TextView) rootView.findViewById(R.id.tvDestination);
        tvVehicleType = (TextView) rootView.findViewById(R.id.tvVehicleType);
        tvVehicleBrandAndModel = (TextView) rootView.findViewById(R.id.tvVehicleBrandAndModel);
        tvAvailableSits = (TextView) rootView.findViewById(R.id.tvAvailableSits);
        tvTime = (TextView) rootView.findViewById(R.id.tvTime);
        tvEmail = (TextView) rootView.findViewById(R.id.tvEmail);
        tvWhatsapp = (TextView) rootView.findViewById(R.id.tvWhatsapp);
        tvPhone = (TextView) rootView.findViewById(R.id.tvPhone);

        loadUserAndSetupUI(rootView);
        return rootView;
    }

    private void loadUserAndSetupUI(final View rootView) {
        if (userId != null) {
            rootView.findViewById(R.id.previewWarn).setVisibility(View.GONE);
            rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            NetworkHelper.getInstance(getActivity()).getRideById(userId, new ResponseCallback() {
                @Override
                public void onSuccess(String jsonStringResponse) {
                    rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                        if (jsonObject.getBoolean("status")) {
                            Ride ride = new Ride(jsonObject.getJSONObject("data"));
                            setupUI(ride);
                        } else {
                            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                            ((MessageDeliveredListener) getActivity()).onMessageDelivered("Falha ao exibir carona", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.ERROR);
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(VolleyError error) {
                    rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    ((MessageDeliveredListener) getActivity()).onMessageDelivered("Falha ao exibir carona", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.ERROR);
                    getActivity().getSupportFragmentManager().popBackStack();
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
        TODO: verificar se existe uma carona pra esse usuário e exibir (??)
         */
        Toast.makeText(getActivity(), "Carrega local", Toast.LENGTH_LONG).show();
    }

    /**
     * Carrega a view com dados da carona trazidas do banco
     *
     * @param ride - Carona que vai preencher a tela
     */
    private void setupUI(Ride ride) {
        /**
         TODO: Vai ser preciso ter uma tela pra perfil e uma tela pra mostrar carona
         O motorista vem dentro da carona. Se não tiver carona, não tem como esconder a mesange de que
         existe carona aberta e exibir só os dados do motorista (porque não haverá carona).

         A tela de exibição de perfil deve consultar somente o usuário por id.
         */

        if (ride.getDriver().getPhotoUrl() != null) {
            ivUrlProfile.setImageURI(Uri.parse(ride.getDriver().getPhotoUrl()));
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 83, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        vStarsCounter.setLayoutParams(layoutParams);
        btShowEvaluations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Evaluation set to 83%", Toast.LENGTH_SHORT).show();
            }
        });
        tvName.setText(ride.getDriver().getFullName());
        tvOrigin.setText(ride.getOrigin());
        if (ride.getOriginComplement().isEmpty()) {
            tvComplement.setVisibility(View.GONE);
        } else {
            tvComplement.setText(ride.getOriginComplement());
        }
        tvDestination.setText(ride.getDestinationNeighborhood() + " - " + ride.getDestinationCity());

        if (ride.getDriver().getActiveCar().getType() == Vehicle.VEHICLE_TYPE_MOTO) {
            tvVehicleType.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), R.drawable.ic_moto_black), null, null, null);
        } else {
            tvVehicleType.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), R.drawable.ic_car_black), null, null, null);
        }

        tvVehicleBrandAndModel.setText(ride.getDriver().getActiveCar().getModel() + " - " + ride.getDriver().getActiveCar().getBrand());

        switch (ride.getAvailableSits()){
            case 0:
                tvAvailableSits.setText("(Lotada)");
                tvAvailableSits.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorSecondary));
                btGetRide.setSupportBackgroundTintList(ContextCompat.getColorStateList(getActivity(), android.R.color.darker_gray));
                btGetRide.setText("CARONA CHEIA");
                btGetRide.setEnabled(false);
                break;
            case 1:
                tvAvailableSits.setText("("+ride.getAvailableSits() + " vaga)");
                break;
            default:
                tvAvailableSits.setText("("+ride.getAvailableSits() + " vagas)");
        }

        tvTime.setText(ride.getDepartTimeString());
        tvEmail.setText(ride.getDriver().getEmail());
        tvWhatsapp.setText(ride.getDriver().getWhatsapp());
        tvPhone.setText(ride.getDriver().getPhone());
    }
}
