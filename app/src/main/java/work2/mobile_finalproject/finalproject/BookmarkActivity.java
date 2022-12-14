package work2.mobile_finalproject.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    ListView lvBookmark = null;
    BookmarkDBHelper helper;
    PlaceDBManager placeDBManager;
    Cursor cursor;
    BookmarkAdapter adapter;
    ArrayList<PlaceDto> arrayList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        lvBookmark = (ListView)findViewById(R.id.lvBookmark);
        arrayList = new ArrayList();
        helper = new BookmarkDBHelper(this);
        placeDBManager = new PlaceDBManager(this);

        adapter = new BookmarkAdapter(this, R.layout.listview_item1, null);
        lvBookmark.setAdapter(adapter);

        lvBookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SQLiteDatabase db = helper.getWritableDatabase();

                String selection = BookmarkDBHelper.COL_ID + "=?";
                String[] selectArgs = new String[]{String.valueOf(id)};

                Cursor cursor = db.query(BookmarkDBHelper.TABLE_NAME, null, selection, selectArgs,
                        null,null,null,null);

                PlaceDto placeDto = new PlaceDto();
                while(cursor.moveToNext()){
                    placeDto.setId(Long.parseLong(cursor.getString(cursor.getColumnIndex(BookmarkDBHelper.COL_ID))));
                    placeDto.setName(cursor.getString(cursor.getColumnIndex(BookmarkDBHelper.COL_NAME)));
                    String phone;
                    try{
                        phone = cursor.getString(cursor.getColumnIndex(BookmarkDBHelper.COL_PHONE));
                    }catch(NullPointerException e){
                        phone = "???????????? ????????? ????????????.";
                    }
                    placeDto.setPhone(phone);
                    placeDto.setAddress(cursor.getString(cursor.getColumnIndex(BookmarkDBHelper.COL_ADDRESS)));
                    placeDto.setPlaceId(cursor.getString(cursor.getColumnIndex(BookmarkDBHelper.COL_PLACEID)));
                    placeDto.setLat(cursor.getDouble(cursor.getColumnIndex(BookmarkDBHelper.COL_LAT)));
                    placeDto.setLng(cursor.getDouble(cursor.getColumnIndex(BookmarkDBHelper.COL_LNG)));
                    placeDto.setKeyWord(cursor.getString(cursor.getColumnIndex(BookmarkDBHelper.COL_KEYWORD)));
                }
                Intent intent = new Intent(BookmarkActivity.this, BookmarkPlaceActivity.class);
                intent.putExtra("bookmarkData", placeDto);
                startActivity(intent);
            }
        });

        // ?????? ????????? ?????? ?????? ?????? ????????? ????????? ???????????? ??? ???????????????
        lvBookmark.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BookmarkActivity.this);
                builder.setTitle("?????? ??????")
                        .setMessage("?????????????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (placeDBManager.deleteBookmark(id)) {
                                    Toast.makeText(BookmarkActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
                                    getBookmarkList(); // ???????????? ?????? ?????? ??? ?????? ???????????? ?????????
                                } else {
                                    Toast.makeText(BookmarkActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("??????", null)
                        .setCancelable(false)
                        .show();
                return true;
            }
        });

        this.addDrawerMenu();
    }

    protected void onResume() {
        super.onResume();
        getBookmarkList(); // ?????? ?????????

        helper.close();
    }

    protected void getBookmarkList(){
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + BookmarkDBHelper.TABLE_NAME, null);

        adapter.changeCursor(cursor);
        helper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) cursor.close();
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
                AlertDialog.Builder  builder = new AlertDialog.Builder(BookmarkActivity.this);
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
                BookmarkActivity.this,
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
                    Intent intent = new Intent(BookmarkActivity.this, BookmarkActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                }else if(id == R.id.menu_item2){
                    Intent intent = new Intent(BookmarkActivity.this, ReviewActivity.class);
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