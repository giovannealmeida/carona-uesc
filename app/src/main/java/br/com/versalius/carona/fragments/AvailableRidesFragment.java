package br.com.versalius.carona.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import br.com.versalius.carona.R;
import br.com.versalius.carona.adapters.RideAdapter;
import br.com.versalius.carona.interfaces.AddFragmentAsActivity;
import br.com.versalius.carona.interfaces.MessageDeliveredListener;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.models.Ride;
import br.com.versalius.carona.network.NetworkHelper;
import br.com.versalius.carona.network.ResponseCallback;
import br.com.versalius.carona.utils.CustomSnackBar;
import br.com.versalius.carona.utils.ProgressDialogHelper;

public class AvailableRidesFragment extends Fragment {

    //Listeners
    private MessageDeliveredListener messageDeliveredListener; //Cria Snacks na MainActivity
    private OnRideListScrollListener mListener; //Seta o comportamento do FAB na MainActivity

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private ArrayList<Ride> rides;
    private ProgressDialogHelper dialogHelper;

    public static AvailableRidesFragment newInstance() {
        return new AvailableRidesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_available_rides, container, false);
        dialogHelper = new ProgressDialogHelper(getActivity());
        emptyView = rootView.findViewById(R.id.emptyView);

        setUpRecycleView(rootView);
        return rootView;
    }

    private void setUpRecycleView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rvRides);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                emptyView.setVisibility(View.GONE);
                getRides();
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //Seta o comportamento do FAB na MainActivity
                if (mListener != null) {
                    if (dy > 0) {
                        mListener.onScrollDown();
                    } else {
                        mListener.onScrollUp();
                    }
                }

                //Carrega mais itens na lista
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                RideAdapter adapter = (RideAdapter) recyclerView.getAdapter();

                if (rides.size() == layoutManager.findLastCompletelyVisibleItemPosition()+1) {
                    loadMoreRides(adapter);
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        dialogHelper.showSpinner(getString(R.string.progress_wait), getString(R.string.progress_loading_rides), false, false);
        getRides();
    }

    private void getRides(){
        NetworkHelper.getInstance(getActivity()).getRidesByStatus(Ride.RIDE_OPEN, 0, 0, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    dialogHelper.dismiss();
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if (jsonObject.getBoolean("status")) {
                        JSONArray jsonRides = jsonObject.getJSONArray("data");
                        rides = new ArrayList<>();
                        for (int i = 0; i < jsonRides.length(); i++) {
                            rides.add(new Ride(jsonRides.getJSONObject(i)));
                        }

                        RideAdapter adapter = new RideAdapter(rides, getActivity());
                        adapter.setOnItemClickListener(new RecycleViewOnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                Ride ride = ((RideAdapter) recyclerView.getAdapter()).getDataset().get(position);
                                RideFragment fragment = RideFragment.newInstance();
                                Bundle b = new Bundle();
                                b.putString("user_id",String.valueOf(ride.getId()));
                                fragment.setArguments(b);
                                ((AddFragmentAsActivity)getActivity()).onAddFragment(fragment,ride.getDriver().getFullName());
                            }
                        });

                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE);

                    } else { //Não existem caronas
                        ((TextView)emptyView.findViewById(R.id.tvEmpty)).setText(jsonObject.getString("message"));
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((TextView)emptyView.findViewById(R.id.tvEmpty)).setText(R.string.the_server_is_not_okay);
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFail(VolleyError error) {
                dialogHelper.dismiss();
                ((TextView)emptyView.findViewById(R.id.tvEmpty)).setText(R.string.failed_load_rides);
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void loadMoreRides(final RideAdapter adapter) {
        dialogHelper.showSpinner(getString(R.string.progress_wait), getString(R.string.progress_loading_rides), false, false);
        NetworkHelper.getInstance(getActivity()).getRidesByStatus(Ride.RIDE_OPEN, 10, rides.size(), new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    dialogHelper.dismiss();
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if (jsonObject.getBoolean("status")) {
                        JSONArray jsonRides = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonRides.length(); i++) {
                            adapter.addItemList(new Ride(jsonRides.getJSONObject(i)));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                dialogHelper.dismiss();
                messageDeliveredListener.onMessageDelivered(getString(R.string.failed_load_rides), Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR);
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

        if (context instanceof MessageDeliveredListener) {
            messageDeliveredListener = (MessageDeliveredListener) context;
        }else {
            throw new RuntimeException(context.toString()
                    + " must implement MessageDeliveredListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        messageDeliveredListener = null;
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
