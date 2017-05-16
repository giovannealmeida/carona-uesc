package br.com.versalius.carona.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.carona.R;
import br.com.versalius.carona.activities.VehicleSettingsActivity;
import br.com.versalius.carona.fragments.ChangeVehicleFragment;
import br.com.versalius.carona.interfaces.MessageDeliveredListener;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.interfaces.UserUpdateListener;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;
import br.com.versalius.carona.utils.DBHelper;
import br.com.versalius.carona.utils.ProgressDialogHelper;

import static br.com.versalius.carona.fragments.ChangeVehicleFragment.ACTION_EDIT_VEHICLE;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private UserUpdateListener userUpdateListener;
    private MessageDeliveredListener messageDeliveredListener;
    private ChangeVehicleFragment.OnVehicleListChanged onVehicleListChanged;

    private List<Vehicle> list;
    private LayoutInflater inflater;
    private RecycleViewOnItemClickListener onItemClickListener;
    private Context context;
    private Fragment fragment;
    private int currentDefaultVehiclePosition = -1;

    public VehicleAdapter(List<Vehicle> list, Context context, Fragment fragment, ChangeVehicleFragment.OnVehicleListChanged onVehicleListChanged) {
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.fragment = fragment;
        if (context instanceof UserUpdateListener) {
            userUpdateListener = (UserUpdateListener) context;
        }
        if (context instanceof MessageDeliveredListener) {
            messageDeliveredListener = (MessageDeliveredListener) context;
        }
        this.onVehicleListChanged = onVehicleListChanged;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_vehicle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvVehicleBrand.setText(list.get(position).getBrand());
        holder.tvVehicleModel.setText(list.get(position).getModel());

        if (list.get(position).getMainPhotoUrl() != null) {
            holder.ivVehicle.setImageURI(Uri.parse(list.get(position).getMainPhotoUrl()));
        }

        if (list.get(position).isDefault()) {
            currentDefaultVehiclePosition = position;
            holder.btIsNotDefault.setVisibility(View.GONE);
            holder.btIsDefault.setVisibility(View.VISIBLE);
        } else {
            holder.btIsNotDefault.setVisibility(View.VISIBLE);
            holder.btIsDefault.setVisibility(View.GONE);
        }

        holder.btIsNotDefault.setTag(position);
        holder.btIsNotDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentDefaultVehiclePosition >= 0) { //Se existe um veículo default
                    updateMainVehicle(list.get(currentDefaultVehiclePosition), list.get((int) view.getTag()), (int) view.getTag());
                } else { //Se não existe um veículo default
                    updateMainVehicle(null, list.get((int) view.getTag()), (int) view.getTag());
                }
            }
        });
        holder.btIsDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMainVehicle(list.get(currentDefaultVehiclePosition), null, 0);
            }
        });
        holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list.get(position).isDefault()) {
                    messageDeliveredListener.onMessageDelivered(context.getString(R.string.cant_remove_default_vehicle), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.INFO);
                } else {
                    final ProgressDialogHelper dialogHelper = new ProgressDialogHelper(context);
                    dialogHelper.showSpinner(null, context.getString(R.string.removing_vehicle), false, false);
                    NetworkHelper.getInstance(context).removeVehicle(String.valueOf(list.get(position).getId()), new ResponseCallback() {
                        @Override
                        public void onSuccess(String jsonStringResponse) {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                if (jsonObject.getBoolean("status")) {
                                    DBHelper helper = DBHelper.getInstance(context);
                                    helper.getDatabase().delete(DBHelper.TBL_VEHICLE, "id = ?", new String[]{"" + list.get(position).getId()});
                                    helper.getDatabase().delete(DBHelper.TBL_VEHICLE_GALLERY, "vehicle_id = ?", new String[]{"" + list.get(position).getId()});
                                    helper.close();
                                    list.remove(position);
                                    notifyDataSetChanged();
                                    onVehicleListChanged.onVehicleRemoved();
                                    messageDeliveredListener.onMessageDelivered(context.getString(R.string.success_removing_vehicle), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.SUCCESS);
                                } else {
                                    messageDeliveredListener.onMessageDelivered(context.getString(R.string.failed_removing_vehicle), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                messageDeliveredListener.onMessageDelivered(context.getString(R.string.failed_removing_vehicle), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);
                            } finally {
                                dialogHelper.dismiss();
                            }
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            dialogHelper.dismiss();
                            messageDeliveredListener.onMessageDelivered(context.getString(R.string.failed_removing_vehicle), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);
                        }
                    });
                }
            }
        });
        holder.btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.startActivityForResult(new Intent(context, VehicleSettingsActivity.class).putExtra("vehicle", list.get(position)), ACTION_EDIT_VEHICLE);

//                context.startActivity(new Intent(context, VehicleSettingsActivity.class).putExtra("vehicle",list.get(position)));
            }
        });
    }

    /**
     * Atualiza o banco de dados, tornando o novo veículo como o principal e depois atualiza a lista.
     * O botão do novo carro principal deve ser selecionado, para isso a tag do novo botão a ser selecionado
     * é passado por parâmetro.
     * <p>
     * Se o primeiro parâmetro (lastMainVehicle) for null, deve-se atribuir um novo veículo principal, somente.
     * Se o segundo parâmetro (newMainVehicle) for null, deve-se somente remover o veículo principal
     * atual e tornar o usuário um caroneiro.
     *
     * @param previousMainVehicle
     * @param newMainVehicle
     * @param tag
     */
    private void updateMainVehicle(final Vehicle previousMainVehicle, final Vehicle newMainVehicle, final int tag) {
        final String previousId = String.valueOf(previousMainVehicle == null ? null : String.valueOf(previousMainVehicle.getId()));
        final String newId = String.valueOf(newMainVehicle == null ? null : String.valueOf(newMainVehicle.getId()));

        final ProgressDialogHelper helper = new ProgressDialogHelper(context);
        helper.showSpinner(context.getResources().getString(R.string.progress_wait), context.getString(R.string.progress_saving_changes), false, false);

        NetworkHelper.getInstance(context).updateDefaultVehicle(previousId, newId, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if (jsonObject.getBoolean("status")) {
                        ContentValues cv = new ContentValues();
                        DBHelper helper = DBHelper.getInstance(context);
                        SQLiteDatabase db = helper.getDatabase();
                        if (previousMainVehicle != null) {
                            cv.put("is_default", 0);
                            db.update(DBHelper.TBL_VEHICLE, cv, null, null);
                            previousMainVehicle.setDefault(false);
                        }

                        if (newMainVehicle != null) {
                            cv.put("is_default", 1);
                            db.update(DBHelper.TBL_VEHICLE, cv, "id = ?", new String[]{newMainVehicle.getId() + ""});
                            newMainVehicle.setDefault(true);
                            currentDefaultVehiclePosition = tag;
                        } else {
                            currentDefaultVehiclePosition = -1;
                        }

                        helper.close();

                        notifyDataSetChanged();
                        userUpdateListener.OnVehicleUpdate(newMainVehicle);
                    } else {
                        messageDeliveredListener.onMessageDelivered(context.getString(R.string.failed_saving_changes), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    messageDeliveredListener.onMessageDelivered(context.getString(R.string.failed_saving_changes), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);
                } finally {
                    helper.dismiss();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                helper.dismiss();
                messageDeliveredListener.onMessageDelivered(context.getString(R.string.failed_saving_changes), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void setOnItemClickListener(RecycleViewOnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public List<Vehicle> getDataset() {
        return list;
    }

    public void addItem(Vehicle vehicle) {
        if (list == null) {//O usuario nao possui veículo algum
            list = new ArrayList<>();
        }
        //busca pelo veículo na lista
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == vehicle.getId()) {
                //Se encontrou, substitui, recostroi a lista e retorna
                list.set(i, vehicle);
                notifyDataSetChanged();
                return;
            }
        }
        //se não encontrou ao fim do laço, adiciona o novo item e reconstroi a lista
        list.add(vehicle);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvVehicleModel, tvVehicleBrand;
        public ImageButton btDelete, btEdit, btIsDefault, btIsNotDefault;
        public SimpleDraweeView ivVehicle;

        public ViewHolder(View itemView) {
            super(itemView);
            tvVehicleModel = (TextView) itemView.findViewById(R.id.tvVehicleModel);
            tvVehicleBrand = (TextView) itemView.findViewById(R.id.tvVehicleBrand);
            btDelete = (ImageButton) itemView.findViewById(R.id.btDelete);
            btEdit = (ImageButton) itemView.findViewById(R.id.btEdit);
            btIsDefault = (ImageButton) itemView.findViewById(R.id.btIsDefault);
            btIsNotDefault = (ImageButton) itemView.findViewById(R.id.btIsNotDefault);
            ivVehicle = (SimpleDraweeView) itemView.findViewById(R.id.ivVehiclePic);
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            ivVehicle.getHierarchy().setRoundingParams(roundingParams);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }
}
