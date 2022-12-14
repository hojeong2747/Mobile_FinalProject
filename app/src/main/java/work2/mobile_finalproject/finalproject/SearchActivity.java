package work2.mobile_finalproject.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import noman.googleplaces.PlaceType;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback {

    final static String TAG = "SearchActivity";
    final static int PERMISSION_REQ_CODE = 100;

    private GoogleMap mGoogleMap;

    private MarkerOptions markerOptions;
    private Map<String, Marker> markerMap; // ?????? ?????????
    private ArrayList<String> placeList;
    private Map<String, Marker> markerMap2;
    private ArrayList<String> placeList2; // ?????? 10km ?????? ?????????
    private ArrayList<Marker> markersArray;

    private PlacesClient placesClient;
    LatLng currentLoc;

    private EditText etSearch;
    private String keyWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearch);
        markerMap = new HashMap<>();
        placeList = new ArrayList<>();
        markerMap2 = new HashMap<>();
        placeList2 = new ArrayList<>();
        markersArray = new ArrayList<>();

        Intent intent = getIntent();
        currentLoc = intent.getExtras().getParcelable("currentLoc");

        if (checkPermission()) mapLoad();

        Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
        placesClient = Places.createClient(this);

        searchStart(PlaceType.PARK);

        this.addDrawerMenu();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                // ????????? name?????? ???????????? ?????? ????????? GREEN?????? ??????
                keyWord = etSearch.getText().toString();
                if(markerMap2.containsKey(keyWord)) {
//                    Toast.makeText(SearchActivity.this, "if ??? ?????? ?????????", Toast.LENGTH_SHORT).show();
                    for(String key : markerMap2.keySet()) {
                        if(key.equals(keyWord)) {
                            markerMap2.get(key).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            Toast.makeText(SearchActivity.this, "?????? ???????????????.", Toast.LENGTH_SHORT).show();

                            // ?????? ?????? ????????? ??????
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerMap2.get(key).getPosition(), 15));
                        }
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOK:
                Toast.makeText(SearchActivity.this, "?????? 10km ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                searchStartByKeyWord(PlaceType.PARK);
                break;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);

        markerOptions = new MarkerOptions();

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(SearchActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mGoogleMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                Toast.makeText(SearchActivity.this,
                        String.format("?????? ??????: (%f, %f)", location.getLatitude(), location.getLongitude()),
                        Toast.LENGTH_SHORT).show();
            }
        });
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String placeId = marker.getTag().toString();
                getPlaceDetail(placeId);
            }
        });
    }

    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);      // ???????????? this: MainActivity ??? OnMapReadyCallback ??? ???????????????

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mapLoad();
            } else {
                Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // PlaceType.PARK ???????????? ?????? ?????? ??????
    private void searchStart(String type) {
        new NRPlaces.Builder().listener(placesListener)
                .key(getResources().getString(R.string.api_key))
                .latlng(currentLoc.latitude, currentLoc.longitude)
                .radius(10000)
                .type(type)
                .build()
                .execute();
    }

    // ????????? ???????????? ??????
    private void searchStartByKeyWord(String type) {
        // ?????? ?????? ????????? ?????? ??????
        for(int i = 0; i < markersArray.size(); i++){
            markersArray.get(i).remove();
        }

        new NRPlaces.Builder().listener(placesListener2)
                .key(getResources().getString(R.string.api_key))
                .latlng(currentLoc.latitude, currentLoc.longitude)
                .radius(10000) // ?????? ?????? ?????? 10km ??? ??????
                .type(type)
                .keyword("??????")
                .build()
                .execute();
    }

    PlacesListener placesListener = new PlacesListener() {
        int count = 0;
        @Override
        public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
            //?????? ??????
            runOnUiThread(() -> {
                for(Place place : places){
                    markerOptions.title(place.getName())
                            .position(new LatLng(place.getLatitude(),place.getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                    Marker newMarker = mGoogleMap.addMarker(markerOptions);
                    newMarker.setTag(place.getPlaceId()); // Tag??? PlaceId ??????
                    markerMap.put(place.getName(), newMarker);
                    placeList.add(place.getName());
                    Log.d(TAG, place.getName() + "  " + place.getPlaceId());
                    count++;
                }
            });
        }

        @Override
        public void onPlacesFailure(PlacesException e) {
        }
        @Override
        public void onPlacesStart() { }
        @Override
        public void onPlacesFinished() {
            if(count == 0 )
                Toast.makeText(SearchActivity.this, "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    };

    PlacesListener placesListener2 = new PlacesListener() {
        int count = 0;
        @Override
        public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
            runOnUiThread(() -> {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 12));
                for(Place place : places){
                    markerOptions.title(place.getName())
                            .position(new LatLng(place.getLatitude(),place.getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                    Marker newMarker = mGoogleMap.addMarker(markerOptions);
                    newMarker.setTag(place.getPlaceId()); // Tag??? PlaceId ??????
                    markerMap2.put(place.getName(), newMarker);
                    placeList2.add(place.getName());
                    markersArray.add(newMarker);
                    Log.d(TAG, place.getName() + "  " + place.getPlaceId());
                    count++;
                }
            });
        }

        @Override
        public void onPlacesFailure(PlacesException e) {
        }
        @Override
        public void onPlacesStart() { }
        @Override
        public void onPlacesFinished() {
            if(count == 0 )
                Toast.makeText(SearchActivity.this, "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    };

    // newMarker Tag ??? ???????????? placeId??? ????????? ?????? ???????????? ?????? (Field ?????? ?????? - ???????????? ????????? ??????)
    private void getPlaceDetail(String placeId) {
        List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Arrays.asList(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
                com.google.android.libraries.places.api.model.Place.Field.WEBSITE_URI,
                com.google.android.libraries.places.api.model.Place.Field.PHONE_NUMBER,
                com.google.android.libraries.places.api.model.Place.Field.RATING);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {
                com.google.android.libraries.places.api.model.Place place = response.getPlace();
                callDetailActivity(place);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ApiException){
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    Log.e(TAG, "not found " + statusCode + " " + e.getMessage());
                }
            }
        });
    }

    private void callDetailActivity (com.google.android.libraries.places.api.model.Place place) {
        Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
        intent.putExtra("id", place.getId());
        intent.putExtra("name",place.getName());
        intent.putExtra("address",place.getAddress());
        intent.putExtra("uri", place.getWebsiteUri());
        intent.putExtra("phone",place.getPhoneNumber());

        try {
            intent.putExtra("rating", place.getRating());
        }catch (NullPointerException e){
            intent.putExtra("rating", "no rating info");
        }

        intent.putExtra("currentLoc", currentLoc);
        intent.putExtra("keyword", "??????");

        startActivity(intent);
    }

    // menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item01: // ??? ????????????
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.item02: //??? ??????
                AlertDialog.Builder  builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle("??????")
                        .setMessage("?????? ?????????????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                moveTaskToBack(true);
                                finishAndRemoveTask();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        })
                        .setNegativeButton("??????", null)
                        .setCancelable(false)
                        .show();
                break;
        }
        return true;
    }

    public void addDrawerMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        DrawerLayout drawLayout = (DrawerLayout) findViewById(R.id.drawer_menu);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                SearchActivity.this,
                drawLayout,
                toolbar,
                R.string.open,
                R.string.close
        );

        drawLayout.addDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();

                if (id == R.id.menu_item1){
                    Intent intent = new Intent(SearchActivity.this, BookmarkActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                }else if(id == R.id.menu_item2){
                    Intent intent = new Intent(SearchActivity.this, ReviewActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "?????? ??? ??????", Toast.LENGTH_SHORT).show();
                }

                DrawerLayout drawer = findViewById(R.id.drawer_menu);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_menu);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}