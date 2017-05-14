package br.com.versalius.carona.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.carona.R;
import br.com.versalius.carona.activities.VehicleSettingsActivity;
import br.com.versalius.carona.adapters.VehicleAdapter;
import br.com.versalius.carona.interfaces.MessageDeliveredListener;
import br.com.versalius.carona.interfaces.UserUpdateListener;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.utils.DBHelper;
import br.com.versalius.carona.utils.ProgressDialogHelper;

public class ChangeVehicleFragment extends Fragment {

    //Listeners
    private MessageDeliveredListener messageDeliveredListener;
    private UserUpdateListener userUpdateListener;

    private List<Vehicle> vehicles;
    private RecyclerView rvVehicles;
    private View emptyView;
    private View content;

    private boolean isListUpdated = false;
    private VehicleAdapter adapter;

    public static ChangeVehicleFragment newInstance() {
        return new ChangeVehicleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_vehicle, container, false);
        emptyView = rootView.findViewById(R.id.emptyView);
        content = rootView.findViewById(R.id.content);

        LinearLayout llAddVehicle = (LinearLayout) rootView.findViewById(R.id.llAddVehicle);
        llAddVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isListUpdated = false; //Quando a VehicleSettingsActivity fechar, a lista de carros vai ser carregada de novo
                startActivity(new Intent(getActivity(), VehicleSettingsActivity.class));
            }
        });

        AppCompatButton btAddVehicle = (AppCompatButton) rootView.findViewById(R.id.btAddVehicle);
        btAddVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isListUpdated = false;
                startActivity(new Intent(getActivity(), VehicleSettingsActivity.class));
            }
        });

        setUpRecyclerView(rootView);
        return rootView;
    }

    private void setUpRecyclerView(View rootView) {

        rvVehicles = (RecyclerView) rootView.findViewById(R.id.rvVehicles);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvVehicles.setLayoutManager(manager);
        List<Vehicle> list = loadVehicles();
        //Seta o adapter mesmo a lista sendo null. Se um veículo for adicionado só é preciso chamar notifyDatasetChaged()
        adapter = new VehicleAdapter(list, getActivity());
        rvVehicles.setAdapter(adapter);

        if (list == null) {
            content.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private List<Vehicle> loadVehicles() {
        ProgressDialogHelper progressDialog = new ProgressDialogHelper(getActivity());
        progressDialog.showSpinner(null, getString(R.string.progress_loading_vehicles), true, false);
        //Carrega os veículos
        DBHelper helper = DBHelper.getInstance(getActivity().getApplicationContext());
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_VEHICLE, null, null, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            if (vehicles == null) {
                vehicles = new ArrayList<>();
            } else {
                vehicles.clear();
            }
            cursor.moveToFirst();
            do {
                Vehicle vehicle = new Vehicle();
                vehicle.setId(cursor.getInt(0));
                vehicle.setDefault(cursor.getInt(1) == 1);
                vehicle.setType(cursor.getInt(2));
                vehicle.setModel(cursor.getString(3));
                vehicle.setBrand(cursor.getString(4));
                vehicle.setAir(cursor.getInt(5) == 1);
                vehicle.setNumDoors(cursor.getInt(6));
                vehicle.setNumSits(cursor.getInt(7));
                vehicle.setPlate(cursor.getString(8));
                vehicle.setColorName(cursor.getString(9));
                vehicle.setColorHex(cursor.getString(10));
                vehicle.setMainPhotoUrl(cursor.getString(11));

                vehicles.add(vehicle);
            } while (cursor.moveToNext());
        }
        cursor.close();
        helper.close();

        //Se tem veículo, carrega as galerias
        if (vehicles != null) {
            List<String> galleryUrls = new ArrayList<>();
            for (Vehicle vehicle : vehicles) {
                cursor = helper.getDatabase().query(DBHelper.TBL_VEHICLE_GALLERY, null, "vehicle_id = ?", new String[]{vehicle.getId() + ""}, null, null, null, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        galleryUrls.add(cursor.getString(2));
                    } while (cursor.moveToNext());
                    vehicle.setGallery(galleryUrls);
                }
            }
        }
        cursor.close();
        progressDialog.dismiss();
        isListUpdated = true;
        return vehicles;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MessageDeliveredListener) {
            messageDeliveredListener = (MessageDeliveredListener) context;
        }
        if (context instanceof UserUpdateListener) {
            userUpdateListener = (UserUpdateListener) context;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageDeliveredListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isListUpdated) {
            loadVehicles();
            adapter.notifyDataSetChanged();
            if(!adapter.getDataset().isEmpty()){
                emptyView.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
            }
        }
    }
}
