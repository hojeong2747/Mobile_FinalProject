package work2.mobile_finalproject.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    final static String TAG = "DetailActivity";

    TextView tvName;
    TextView tvAddress;
    TextView tvPhone;
    TextView tvUri;
    TextView tvRating;
    ImageView imageView;
    Button btnCall;

    private PlacesClient placesClient;
    private PlaceDBManager placeDBManager;
    private PlaceDto placeDto;
    String name;
    String address;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        createNotificationChannel(); // onCreate()?????? ?????? ?????? ??????

        Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
        placesClient = Places.createClient(this);

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvUri = findViewById(R.id.tvUri);
        tvRating = findViewById(R.id.tvRating);
        imageView = findViewById(R.id.ivPhoto);
        btnCall = findViewById(R.id.btnCall);

        placeDto = new PlaceDto();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        tvName.setText(name);
        placeDto.setName(name);

        address = intent.getStringExtra("address");
        tvAddress.setText(address);
        placeDto.setAddress(address);

        String uri = intent.getStringExtra("uri");
        if (uri == null) {
            tvUri.setText("???????????? ????????? ????????????.");
        } else
            tvUri.setText(uri);
        placeDto.setUri(uri);


        phone = intent.getStringExtra("phone");
        if (phone == null) {
            tvPhone.setText("???????????? ????????? ????????????.");
            btnCall.setEnabled(false);
        } else
            tvPhone.setText(phone);
        placeDto.setPhone(phone);


        String rating = String.valueOf(intent.getDoubleExtra("rating", 0.0));
        if (rating.equals("no rating info"))
            tvRating.setText("?????? ????????? ????????????.");
        else
            tvRating.setText(rating);

        // ?????? ??????
        String placeId = intent.getStringExtra("id");
        getPlaceDetail(placeId);
        placeDto.setPlaceId(placeId);

        LatLng currentLoc = intent.getParcelableExtra("currentLoc");
        placeDto.setLat(currentLoc.latitude);
        placeDto.setLng(currentLoc.longitude);
        placeDto.setKeyWord(intent.getStringExtra("keyword"));

        this.addDrawerMenu();
    }

    private void getPlaceDetail(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {
                Place place = response.getPlace();
                // ?????? meta data ?????????
                final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                if (metadata == null || metadata.isEmpty()) {
                    Log.w(TAG, "no data.");
                    imageView.setImageResource(R.mipmap.ic_launcher);
                    return;
                }
                final PhotoMetadata photoMetadata = metadata.get(0);

                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    imageView.setImageBitmap(bitmap);
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "not found " + exception.getMessage());
                        // TODO: Handle error with given status code.
                    }
                });
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCall:
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setTitle("?????? ??????")
                        .setMessage("????????? ???????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ?????? ????????? ????????? ??????
                                Intent intent = new Intent("android.intent.action.DIAL", Uri.parse("tel:" + phone));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("??????", null)
                        .setCancelable(false)
                        .show();

                break;
            case R.id.btnBookMark:
                AlertDialog.Builder markBuilder = new AlertDialog.Builder(DetailActivity.this);
                markBuilder.setTitle("????????????")
                        .setMessage("??????????????? ?????????????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                placeDBManager = new PlaceDBManager(DetailActivity.this);
                                boolean result = placeDBManager.addBookmark(placeDto);
                                if(result) {
                                    // ?????? ??????
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(DetailActivity.this, "MY_CHANNEL")
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle("??????")
                                            .setContentText("???????????? ??????")
                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                    .bigText("???????????? ??????\n??????????????? ?????????????????????."))
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true);

                                    // ?????? ??????
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(DetailActivity.this);
                                    int notificationId = 100; // ?????? ?????? ?????? ????????? ????????? ??????
                                    notificationManager.notify(notificationId, builder.build()); // ?????? ?????? ??????

                                    AlertDialog.Builder moveBuilder = new AlertDialog.Builder(DetailActivity.this);
                                    moveBuilder.setTitle("??????????????? ?????????????????????.")
                                            .setMessage("??????????????? ?????????????????????????")
                                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(DetailActivity.this, BookmarkActivity.class);
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton("??????", null)
                                            .setCancelable(false)
                                            .show();
                                }
                            }
                        })
                        .setNegativeButton("??????", null)
                        .setCancelable(false)
                        .show();
                break; // ??? ?????? ?????? ?????????
            case R.id.btnReview:
                // ?????? ?????? (?????? ???????????? ?????????, ??????, ????????? ???????????????)
                Intent intent = new Intent(DetailActivity.this, ReviewAddActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("address", address);
                intent.putExtra("phone", phone);
                startActivity(intent);
                break;
            case R.id.btnAddSave:
                finish();
                break;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);       // strings.xml ??? ????????? ??????
            String description = getString(R.string.channel_description);       // strings.xml??? ?????? ?????? ??????
            int importance = NotificationManager.IMPORTANCE_DEFAULT;    // ????????? ???????????? ??????
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);    // CHANNEL_ID ??????
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);  // ?????? ??????
            notificationManager.createNotificationChannel(channel);
        }
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
                AlertDialog.Builder  builder = new AlertDialog.Builder(DetailActivity.this);
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
                DetailActivity.this,
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
                    Intent intent = new Intent(DetailActivity.this, BookmarkActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                }else if(id == R.id.menu_item2){
                    Intent intent = new Intent(DetailActivity.this, ReviewActivity.class);
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