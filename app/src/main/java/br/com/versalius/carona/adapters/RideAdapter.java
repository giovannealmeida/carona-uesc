package br.com.versalius.carona.adapters;

import android.content.Context;
import android.media.Image;
import android.support.design.widget.Snackbar;
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
import br.com.versalius.carona.models.Ride;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder>{

    private List<Ride> list;
    private LayoutInflater inflater;
    private Context context;

    public RideAdapter(List<Ride> list, Context context) {
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_ride,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tvDriverName.setText(list.get(position).getDriver().getFullName());
        holder.tvTime.setText(list.get(position).getDepartTimeString());
        holder.tvAvailableSits.setText(String.valueOf(list.get(position).getAvailableSits()));
        holder.tvOrigin.setText(list.get(position).getOrigin());
        holder.tvDestination.setText(list.get(position).getFullDestination());
        holder.ivProfile.setImageResource(list.get(position).getDriver().getPhoto());
        holder.btGetRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"FALTA FAZER",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDriverName, tvOrigin, tvDestination, tvAvailableSits, tvTime;
        public Button btGetRide;
        public ImageView ivProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDriverName = (TextView) itemView.findViewById(R.id.tvDriverName);
            tvOrigin = (TextView) itemView.findViewById(R.id.tvOrigin);
            tvDestination = (TextView) itemView.findViewById(R.id.tvDestination);
            tvAvailableSits = (TextView) itemView.findViewById(R.id.tvAvailableSits);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            btGetRide = (Button) itemView.findViewById(R.id.btGetRide);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        }
    }
}
