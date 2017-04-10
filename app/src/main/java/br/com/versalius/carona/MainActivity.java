package br.com.versalius.carona;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.carona.adapters.RideAdapter;
import br.com.versalius.carona.interfaces.RecycleViewOnItemClickListener;
import br.com.versalius.carona.models.Ride;
import br.com.versalius.carona.models.User;
import br.com.versalius.carona.models.Vehicle;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private FloatingActionMenu fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.item_menu_available_rides));
        setSupportActionBar(toolbar);

        setUpFabs();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUpRecycleView();
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

    private void setUpRecycleView() {
        recyclerView = (RecyclerView) findViewById(R.id.rvRides);
        recyclerView.setHasFixedSize(true);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    fab.hideMenuButton(true);
                } else {
                    fab.showMenuButton(true);
                }
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        RideAdapter adapter = new RideAdapter(getRides(), this);
        adapter.setOnItemClickListener(new RecycleViewOnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(MainActivity.this, "click: " + ((RideAdapter)recyclerView.getAdapter()).getDataset().get(position).getId(), Toast.LENGTH_LONG).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private List<Ride> getRides() {
        Vehicle car1, car2, car3, car4, car5, moto;
        car1 = new Vehicle(1, Vehicle.VEHICLE_TYPE_CAR, "Volkswagen", "Fusca", false, 2, 3, "ABC-1234", "Vermelho", "#FF0000", null, null);
        car2 = new Vehicle(2, Vehicle.VEHICLE_TYPE_CAR, "Volkswagen", "CrossFox", true, 4, 4, "DEF-5687", "Branco", "#FFFFFF", null, null);
        car3 = new Vehicle(3, Vehicle.VEHICLE_TYPE_CAR, "FIAT", "Uno", false, 4, 2, "DEF-5687", "Preto", "#000000", null, null);
        car4 = new Vehicle(4, Vehicle.VEHICLE_TYPE_CAR, "Chevrolet", "Cobalt", true, 4, 4, "DEF-5687", "Laranja", "#FF5000", null, null);
        car5 = new Vehicle(5, Vehicle.VEHICLE_TYPE_CAR, "Mitsibushi", "Pajero", true, 4, 1, "DEF-5687", "Amarelo", "#FFFF00", null, null);
        moto = new Vehicle(6, Vehicle.VEHICLE_TYPE_MOTO, "Honda", "Pop 100", false, 0, 1, "DEF-5687", "Azul", "#0000FF", null, null);

        User driver1, driver2, driver3, driver4, driver5, driver6;
        driver1 = new User(1, car1, "Jorge", "Andrade", "Ilhéus", "N. Sra. da Vitória", null, "carlos.andrade@email.com", "1234", R.drawable.profile_circle3);
        driver2 = new User(2, car2, "Paula", "Cardoso", "Itabuna", "São Caetano", null, "paula.cardoso@email.com", "1234", R.drawable.ic_profile_placeholder);
        driver3 = new User(3, car3, "Alessandra", "Borges", "Itabuna", "Sarinha", null, "alessandra.broges@email.com", "1234", R.drawable.profile_circle);
        driver4 = new User(4, car4, "Carine", "Jade", "Ilhéus", "Centro", null, "carine.jade@email.com", "1234", R.drawable.ic_profile_placeholder);
        driver5 = new User(5, car5, "Juliana", "Britto", "Ilhéus", "Pacheco", null, "juliana.britto@email.com", "1234", R.drawable.profile_circle5);
        driver6 = new User(6, moto, "Matheus", "Almeida", "Ilhéus", "Olivença", null, "metheus.almeida@email.com", "1234", R.drawable.ic_profile_placeholder);

        User passenger1, passenger2, passenger3, passenger4, passenger5;
        passenger1 = new User(7, null, "Adriana", "Silva", "Ilhéus", "N. Sra. da Vitória", null, "adriana.silva@email.com", "1234", R.drawable.ic_profile_placeholder);
        passenger2 = new User(8, null, "Paulo", "Machado", "Ilhéus", "Pontal", null, "paulo.machado@email.com", "1234", R.drawable.profile_circle4);
        passenger3 = new User(9, null, "Bruno", "Azevedo", "Itabuna", "Centro", null, "bruno.azevedo@email.com", "1234", R.drawable.ic_profile_placeholder);
        passenger4 = new User(10, null, "Flávia", "Dias", "Itabuna", "São Pedro", null, "flavia.dias@email.com", "1234", R.drawable.profile_circle2);
        passenger5 = new User(11, null, "Lucas", "Freire", "Ilhéus", "Centro", null, "lucas.freire@email.com", "1234", R.drawable.ic_profile_placeholder);

        List<User> passengers1, passengers2;
        passengers1 = new ArrayList<>();
        passengers2 = new ArrayList<>();

        passengers1.add(passenger1);
        passengers1.add(passenger2);
        passengers1.add(passenger3);

        passengers2.add(passenger4);
        passengers2.add(passenger5);

        Ride ride1, ride2, ride3, ride4, ride5, ride6;
        ride1 = new Ride(1, driver1, passengers1, Ride.RIDE_OPEN, 0, "Guarita", "Ilhéus", "N. Sra. da Vitória", null);
        ride2 = new Ride(2, driver2, null, Ride.RIDE_OPEN, 0, "Pav. Jorge Amado", "Ilhéus", "Pacheco", null);
        ride3 = new Ride(3, driver3, null, Ride.RIDE_OPEN, 0, "Guarita", "Itabuna", "Sarinha", null);
        ride4 = new Ride(4, driver4, passengers2, Ride.RIDE_OPEN, 0, "Biblioteca", "Itabuna", "Centro", null);
        ride5 = new Ride(5, driver5, null, Ride.RIDE_OPEN, 0, "Pav. Jorge Amado", "Itabuna", "Pontalzinho", null);
        ride6 = new Ride(6, driver6, null, Ride.RIDE_OPEN, 0, "Pav. Max de Menezes", "Ilhéus", "Centro", null);

        List<Ride> rides = new ArrayList<>();
        rides.add(ride1);
        rides.add(ride2);
        rides.add(ride3);
        rides.add(ride4);
        rides.add(ride5);
        rides.add(ride6);

        return rides;
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
        if (id == R.id.action_settings) {
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
            // Handle the camera action
        } else if (id == R.id.nav_my_profile) {

        } else if (id == R.id.nav_my_ride) {

        } else if (id == R.id.nav_change_vehicle) {

        } else if (id == R.id.nav_my_evaluations) {

        } else if (id == R.id.nav_ride_history) {

        } else if (id == R.id.nav_account_settings) {

        } else if (id == R.id.nav_donate) {

        } else if (id == R.id.nav_send_email) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
