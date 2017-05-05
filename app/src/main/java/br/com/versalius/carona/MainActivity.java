package br.com.versalius.carona;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import br.com.versalius.carona.activities.AccountActivity;
import br.com.versalius.carona.activities.LoginActivity;
import br.com.versalius.carona.adapters.RideAdapter;
import br.com.versalius.carona.fragments.AccountSettingsFragment;
import br.com.versalius.carona.fragments.AvailableRidesFragment;
import br.com.versalius.carona.models.User;
import br.com.versalius.carona.models.Vehicle;
import br.com.versalius.carona.utils.SessionHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AvailableRidesFragment.OnRideListScrollListener {

    private FloatingActionMenu fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.item_menu_available_rides));
        setSupportActionBar(toolbar);

        setUpFabs();
        setUpDrawer(toolbar);
        showFragment(AvailableRidesFragment.newInstance());
    }

    private void setUpDrawer(Toolbar toolbar) {
        //Recupera usuário
        User user = (User) getIntent().getExtras().getSerializable("user");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Seta o header
        //Nome do usuário
        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main);
        ((TextView) navHeader.findViewById(R.id.tvUsername)).setText(user.getFullName());
        //Foto do usuário
        SimpleDraweeView draweeView = (SimpleDraweeView) navHeader.findViewById(R.id.ivProfile);
        if (user.getPhotoUrl() != null) {
            Uri uri = Uri.parse(user.getPhotoUrl());
            draweeView.setImageURI(uri);
        }
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        draweeView.getHierarchy().setRoundingParams(roundingParams);
        TextView tvCurrentVehicle = (TextView) navHeader.findViewById(R.id.tvCurrentVehicle);
        Vehicle vehicle = user.getActiveCar();
        if (vehicle != null) {
            //Marca e modelo do carro
            tvCurrentVehicle.setText(vehicle.getBrand() + " - " + vehicle.getModel());
            if (vehicle.getType() == Vehicle.VEHICLE_TYPE_MOTO) {
                tvCurrentVehicle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_moto_white, 0, 0, 0);
            } else {
                tvCurrentVehicle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_car_white, 0, 0, 0);
            }
        } else {
            tvCurrentVehicle.setText(getResources().getString(R.string.lb_passenger));
        }
    }

    private void setUpFabs() {
        fab = (FloatingActionMenu) findViewById(R.id.fab);
        fab.setIconAnimated(false);
        fab.setClosedOnTouchOutside(true);
        final FloatingActionButton fabCar = (FloatingActionButton) findViewById(R.id.fabCar);
        final FloatingActionButton fabMoto = (FloatingActionButton) findViewById(R.id.fabMoto);

        fabCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Carro!", Toast.LENGTH_LONG).show();
            }
        });

        fabMoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Moto!", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fab.isOpened()) {
            fab.close(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LogoutDialog dialog = new LogoutDialog();
            dialog.setMessage(getString(R.string.dialog_message_logout));
            dialog.show(this.getSupportFragmentManager(), "dialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_available_rides) {
            showFragment(AvailableRidesFragment.newInstance());
        } else if (id == R.id.nav_my_profile) {

        } else if (id == R.id.nav_my_ride) {

        } else if (id == R.id.nav_change_vehicle) {

        } else if (id == R.id.nav_my_evaluations) {

        } else if (id == R.id.nav_ride_history) {

        } else if (id == R.id.nav_account_settings) {
            showFragment(AccountSettingsFragment.newInstance());
        } else if (id == R.id.nav_donate) {

        } else if (id == R.id.nav_send_email) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onScrollDown() {
        fab.hideMenuButton(true);
    }

    @Override
    public void onScrollUp() {
        fab.showMenuButton(true);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    public static class LogoutDialog extends DialogFragment {

        private String title;
        private String message;

        static LogoutDialog newInstance() {
            return new LogoutDialog();
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_default, null);
            ((TextView) view.findViewById(R.id.tvMessage)).setText(message);

            return new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setCancelable(true)
                    .setTitle(title)
                    .setPositiveButton(getString(R.string.dialog_action_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new SessionHelper(getActivity()).logout();
                                    getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                                    getActivity().finish();
                                }
                            }
                    )
                    .setNeutralButton(getString(R.string.dialog_action_cancel), null)
                    .create();
        }
    }


}
