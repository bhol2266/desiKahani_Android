package com.bhola.desiKahaniya;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Collection_detail extends AppCompatActivity {


    ProgressBar progressBar2;
    TextView check_Internet_Connection;
    Button retryBtn;
    List<Object> collectonData;
    StoryDetails_Adapter adapter2;
    DatabaseReference mref2;
    String Collection_DB_Table_Name, Ads_State, title_category;
    ImageView back, share_ap;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initviews_Check_Internet_Connectivity_Actionbar();
            }
        },10);

        actionBar();

    }


    private void initviews_Check_Internet_Connectivity_Actionbar() {


        recyclerView = findViewById(R.id.recyclerView);

        collectonData = new ArrayList<Object>();
        progressBar2 = findViewById(R.id.progressBar2);
        check_Internet_Connection = findViewById(R.id.check_Internet_Connection);
        retryBtn = findViewById(R.id.retryBtn);


        if (isInternetAvailable(Collection_detail.this)) {
//
            Send_ALL_DATA_TO_RECYCLERVIEW();


        } else {
            check_Internet_Connection.setVisibility(View.VISIBLE);
            check_Internet_Connection.setText("No Internet Connection");
            retryBtn.setVisibility(View.VISIBLE);
        }

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

        });


    }


    private void Send_ALL_DATA_TO_RECYCLERVIEW() {


        if (SplashScreen.Login_Times < 4) {

            String DB_TABLE = getIntent().getStringExtra("Collection_DB_Table_Name");
            if (DB_TABLE.equals("Collection1") || DB_TABLE.equals("Collection2") || DB_TABLE.equals("Collection3") || DB_TABLE.equals("Collection6")) {

                if (DB_TABLE.equals("Collection1")) {
                    getDataFromDatabase_loveStory("Collection7");
                    Collection_DB_Table_Name = "Collection7";
                }
                if (DB_TABLE.equals("Collection2")) {
                    getDataFromDatabase_loveStory("Collection8");
                    Collection_DB_Table_Name = "Collection8";
                }

                if (DB_TABLE.equals("Collection3")) {
                    getDataFromDatabase_loveStory("Collection9");
                    Collection_DB_Table_Name = "Collection9";
                }

                if (DB_TABLE.equals("Collection6")) {
                    getDataFromDatabase_loveStory("Collection10");
                    Collection_DB_Table_Name = "Collection10";
                }

            } else {
                // Collection-4, Collection-5 are Desi Kahanis
                getDataFromDatabase();
            }


        } else {
            // If user is logged in more than 4 time all Collection Will be desi Kahanis
            getDataFromDatabase();
        }

        recyclerView.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.GONE);

        if (!SplashScreen.Sex_Story.equals("active") || !SplashScreen.Sex_Story_Switch_Open.equals("active")) {
            collectonData.clear();
            getDataFromDatabaselovestory();


        }

        adapter2 = new StoryDetails_Adapter(collectonData, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter2);
        adapter2.notifyDataSetChanged();

    }


    private void getDataFromDatabase() {
        List<Object> collectonDataTemp = new ArrayList<>();
        Cursor cursor = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, Collection_DB_Table_Name).readalldata();
        try {
            while (cursor.moveToNext()) {
                FirebaseData firebaseData = new FirebaseData(cursor.getInt(0), cursor.getString(1), "cursor.getString(2)", cursor.getString(2), cursor.getInt(3), Collection_DB_Table_Name);
                collectonDataTemp.add(firebaseData);

            }

            if (SplashScreen.Login_Times < 6) {
                for (int i = 0; i < 10; i++) {
                    collectonData.add(collectonDataTemp.get(i));
                }
            } else {
                collectonData = collectonDataTemp;
            }

            Collections.shuffle(collectonData);

        } finally {
            cursor.close();
        }

    }

    private void getDataFromDatabaselovestory() {


        Cursor cursor = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "Collection7").readalldata();
        try {
            while (cursor.moveToNext()) {
                if (collectonData.size() < 3) { //Add only 3 storys

                    FirebaseData firebaseData = new FirebaseData(cursor.getInt(0), cursor.getString(1), "cursor.getString(2)", cursor.getString(2), cursor.getInt(3), "Collection7");
                    collectonData.add(firebaseData);
                }
            }
            Collections.shuffle(collectonData);
        } finally {
            cursor.close();
        }


    }


    private void getDataFromDatabase_loveStory(String table) {


        try {
            Cursor cursor = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, table).readalldata();
            try {
                while (cursor.moveToNext()) {
                    FirebaseData firebaseData = new FirebaseData(cursor.getInt(0), cursor.getString(1), "cursor.getString(2)", cursor.getString(2), cursor.getInt(3), table);
                    collectonData.add(firebaseData);

                }


            } finally {
                cursor.close();
            }

        } catch (Exception e) {

        }


    }


    boolean isInternetAvailable(Context context) {
        if (context == null) return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut", "Network is available : FALSE ");
        return false;
    }


    private void actionBar() {
        mref2 = FirebaseDatabase.getInstance().getReference();

        TextView title;
        title_category = getIntent().getStringExtra("bhola2");
        title = findViewById(R.id.title_collection);
        Collection_DB_Table_Name = getIntent().getStringExtra("Collection_DB_Table_Name");
        title.setText(title_category);
        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EditText passwordEdittext;
                Button passwordLoginBtn;


                AlertDialog dialog;

                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View promptView = inflater.inflate(R.layout.admin_panel_entry, null);
                builder.setView(promptView);
                builder.setCancelable(true);


                passwordEdittext = promptView.findViewById(R.id.passwordEdittext);
                passwordLoginBtn = promptView.findViewById(R.id.passwordLoginBtn);

                passwordLoginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (passwordEdittext.getText().toString().equals("5555")) {
                            startActivity(new Intent(getApplicationContext(), admin_panel.class));

                        } else {
                            Toast.makeText(v.getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                dialog = builder.create();
                dialog.show();
                return false;
            }
        });
        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        share_ap = findViewById(R.id.share_app);
        share_ap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_ap.setImageResource(R.drawable.favourite_active);
                Intent intent = new Intent(getApplicationContext(), Download_Detail.class);
                intent.putExtra("Ads_Status", Ads_State);
                startActivity(intent);
            }
        });
    }


}


