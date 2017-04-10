package br.com.versalius.carona.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.com.versalius.carona.R;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.models.Ride;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.utils.PreferencesHelper;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {

    private List<Ride> list;
    private LayoutInflater inflater;
    private Context context;
    private RecycleViewOnItemClickListener listener;

    public RideAdapter(List<Ride> list, Context context) {
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_ride, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvDriverName.setText(list.get(position).getDriver().getFullName());
        holder.tvDriverName.setSelected(true);

        holder.tvTime.setText(list.get(position).getDepartTimeString());
        holder.tvAvailableSits.setText(String.valueOf(list.get(position).getAvailableSits()));
        if(list.get(position).getDriver().getVehicle().getType() == Vehicle.VEHICLE_TYPE_MOTO){
            holder.tvAvailableSits.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.ic_moto_black),null,null,null);
        }

        holder.tvOrigin.setText(list.get(position).getOrigin());
        holder.tvOrigin.setSelected(true);

        holder.tvDestination.setText(list.get(position).getFullDestination());
        holder.tvDestination.setSelected(true);

        holder.ivProfile.setImageResource(list.get(position).getDriver().getPhoto());
        if (list.get(position).getAvailableSits() == 0) {
            holder.btGetRide.setEnabled(false);
            holder.btGetRide.setText("Cheia");
//            holder.btGetRide.setBackgroundColor(Color.GRAY);
            holder.btGetRide.setSupportBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
        }

        if (isInRide(list.get(position).getId())) {
            holder.btGetRide.setText("Sair");
//            holder.btGetRide.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary));
            holder.btGetRide.setSupportBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSecondary));
        }

        holder.btGetRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferencesHelper.getInstance(context).load("currentRideId").isEmpty()) {
                    holder.btGetRide.setText("Sair");
//                    holder.btGetRide.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary));
                    holder.btGetRide.setSupportBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSecondary));
                    try {
                        PreferencesHelper.getInstance(context).save("currentRideId", String.valueOf(list.get(position).getId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (isInRide(list.get(position).getId())) {
                    holder.btGetRide.setText("Entrar");
//                    holder.btGetRide.setBackgroundColor(ContextCompat.getColor(context, R.color.colorButton));
                    holder.btGetRide.setSupportBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorButton));

                    try {
                        PreferencesHelper.getInstance(context).save("currentRideId", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Você já está numa carona", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isInRide(int rideId) {
        return PreferencesHelper.getInstance(context).load("currentRideId").equals(String.valueOf(rideId));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void setOnItemClickListener(RecycleViewOnItemClickListener listener){
        this.listener = listener;
    }

    public List<Ride> getDataset(){
        return list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDriverName, tvOrigin, tvDestination, tvAvailableSits, tvTime;
        public AppCompatButton btGetRide;
        public ImageView ivProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDriverName = (TextView) itemView.findViewById(R.id.tvDriverName);
            tvOrigin = (TextView) itemView.findViewById(R.id.tvOrigin);
            tvDestination = (TextView) itemView.findViewById(R.id.tvDestination);
            tvAvailableSits = (TextView) itemView.findViewById(R.id.tvAvailableSits);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            btGetRide = (AppCompatButton) itemView.findViewById(R.id.btGetRide);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        }
    }
}
