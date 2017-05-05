package br.com.versalius.carona.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.carona.MainActivity;
import br.com.versalius.carona.R;
import br.com.versalius.carona.adapters.RideAdapter;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.models.Ride;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;

public class AvailableRidesFragment extends Fragment {

    private OnRideListScrollListener mListener;
    private RecyclerView recyclerView;
    private TextView emptyView;

    public static AvailableRidesFragment newInstance() {
        return new AvailableRidesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_available_rides, container, false);
        emptyView = (TextView) rootView.findViewById(R.id.emptyView);

        setUpRecycleView(rootView);
        return rootView;
    }

    private void setUpRecycleView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvRides);
        recyclerView.setHasFixedSize(true);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mListener != null) {
                    if (dy > 0) {
                        mListener.onScrollDown();
                    } else {
                        mListener.onScrollUp();
                    }
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        NetworkHelper.getInstance(getActivity()).getRidesByStatus(Ride.RIDE_OPEN, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if (jsonObject.getBoolean("status")) {
                        JSONArray jsonRides = jsonObject.getJSONArray("data");
                        List<Ride> rides = new ArrayList<>();
                        for (int i = 0; i < jsonRides.length(); i++) {
                            rides.add(new Ride(jsonRides.getJSONObject(i)));
                        }

                        RideAdapter adapter = new RideAdapter(rides, getActivity());
                        adapter.setOnItemClickListener(new RecycleViewOnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                Toast.makeText(getActivity(), "click: " + ((RideAdapter) recyclerView.getAdapter()).getDataset().get(position).getId(), Toast.LENGTH_LONG).show();
                            }
                        });

                        recyclerView.setAdapter(adapter);
                    } else { //Não existem caronas
                        emptyView.setText(jsonObject.getString("message"));
                        emptyView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                emptyView.setText("Não foi possível carregar as caronas. Tente novamente mais tarde.");
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRideListScrollListener) {
            mListener = (OnRideListScrollListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRideListScrollListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRideListScrollListener {
        void onScrollDown();
        void onScrollUp();
    }
}
