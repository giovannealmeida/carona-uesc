package br.com.versalius.carona.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import br.com.versalius.carona.R;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.models.Vehicle;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

    private List<Vehicle> list;
    private LayoutInflater inflater;
    private RecycleViewOnItemClickListener listener;
    private int defaultVehiclePosition = 0;

    public VehicleAdapter(List<Vehicle> list, Context context) {
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            defaultVehiclePosition = position;
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
                list.get(defaultVehiclePosition).setDefault(false);
                list.get((int)view.getTag()).setDefault(true);
                defaultVehiclePosition = (int) view.getTag();
                notifyDataSetChanged();
            }
        });
        holder.btIsDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void setOnItemClickListener(RecycleViewOnItemClickListener listener){
        this.listener = listener;
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
            if(listener != null){
                listener.onItemClick(v,getAdapterPosition());
            }
        }
    }
}
