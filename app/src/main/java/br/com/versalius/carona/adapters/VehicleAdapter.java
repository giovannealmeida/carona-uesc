package br.com.versalius.carona.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.com.versalius.carona.R;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.interfaces.UserUpdateListener;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;
import br.com.versalius.carona.utils.DBHelper;
import br.com.versalius.carona.utils.ProgressDialogHelper;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private List<Vehicle> list;
    private LayoutInflater inflater;
    private RecycleViewOnItemClickListener onItemClickListener;
    private UserUpdateListener userUpdateListener;
    private Context context;
    private int currentDefaultVehiclePosition = 0;

    public VehicleAdapter(List<Vehicle> list, Context context) {
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        if(context instanceof UserUpdateListener){
            userUpdateListener = (UserUpdateListener) context;
        }
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

       if(list.get(position).getMainPhotoUrl() != null) {
            holder.ivVehicle.setImageURI(Uri.parse(list.get(position).getMainPhotoUrl()));
        }

        if(list.get(position).isDefault()){
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
                updateMainVehicle(list.get(currentDefaultVehiclePosition),list.get((int)view.getTag()),(int) view.getTag());
            }
        });
        holder.btIsDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,context.getString(R.string.this_is_already_main_vehicle),Toast.LENGTH_LONG).show();
            }
        });
        holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    /**
     * Atualiza o banco de dados, tornando o novo veículo como o principal e depois atualiza a lista.
     * O botão do novo carro principal deve ser selecionado, para isso a tag do novo botão a ser selecionado
     * é passado por parâmetro.
     *
     * @param previousMainVehicle
     * @param newMainVehicle
     * @param tag
     */
    private void updateMainVehicle(final Vehicle previousMainVehicle, final Vehicle newMainVehicle, final int tag) {
        final ProgressDialogHelper helper = new ProgressDialogHelper(context);
        helper.showSpinner(context.getResources().getString(R.string.progress_wait),context.getString(R.string.progress_saving_changes),false,false);

        NetworkHelper.getInstance(context).updateDefaultVehicle(String.valueOf(previousMainVehicle.getId()),String.valueOf(newMainVehicle.getId()), new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if(jsonObject.getBoolean("status")){
                        ContentValues cv = new ContentValues();
                        DBHelper helper = DBHelper.getInstance(context);
                        SQLiteDatabase db = helper.getDatabase();
                        cv.put("is_default",0);
                        db.update(DBHelper.TBL_VEHICLE,cv,null,null);

                        cv.put("is_default",1);
                        db.update(DBHelper.TBL_VEHICLE,cv,"id = ?",new String[]{newMainVehicle.getId()+""});

                        helper.close();

                        previousMainVehicle.setDefault(false);
                        newMainVehicle.setDefault(true);
                        currentDefaultVehiclePosition = tag;
                        notifyDataSetChanged();
                        userUpdateListener.OnVehicleUpdate(newMainVehicle);
                    } else {
                        new MaterialDialog.Builder(context)
                                .content(context.getResources().getString(R.string.failed_saving_changes))
                                .neutralText(R.string.dialog_action_ok)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new MaterialDialog.Builder(context)
                            .content(context.getResources().getString(R.string.failed_saving_changes))
                            .neutralText(R.string.dialog_action_ok)
                            .show();
                } finally {
                    helper.dismiss();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                helper.dismiss();
                new MaterialDialog.Builder(context)
                        .content(context.getResources().getString(R.string.failed_saving_changes))
                        .neutralText(R.string.dialog_action_ok)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void setOnItemClickListener(RecycleViewOnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    public List<Vehicle> getDataset(){
        return list;
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
            if(onItemClickListener != null){
                onItemClickListener.onItemClick(v,getAdapterPosition());
            }
        }
    }
}
