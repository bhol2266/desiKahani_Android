package com.bhola.desiKahaniya;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class Download_Detail extends AppCompatActivity {


    String Ads_State;
    List<Object> collectonData;
    public static StoryDetails_Adapter adapter2;
    String message;
    ImageView back;
    private AdView mAdView;
    RecyclerView recyclerView;
    TextView emptyView;
    ProgressBar progressBar2;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        if (SplashScreen.Ads_State.equals("active")) {
            showAds(SplashScreen.Ad_Network_Name, this);
        }

        actionBar();
        initViews();

        progressBar2 = findViewById(R.id.progressBar2);
        progressBar2.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectonData = new ArrayList<Object>();


        String[] Table_Names = {"Collection1", "Collection2", "Collection3", "Collection4", "Collection5", "Collection6", "Collection7", "Collection8", "Collection9", "Collection10"};

        for (int i = 0; i < Table_Names.length; i++) {
            getDataFromDatabase(Table_Names[i]);
        }

        checkCollectionDataEmpty();


        adapter2 = new StoryDetails_Adapter(collectonData, this);
        recyclerView.setAdapter(adapter2);
        adapter2.notifyDataSetChanged();


    }

    private void showAds(String Ad_Network_Name, Context mContext) {


        if (Ad_Network_Name.equals("admob")) {
            mAdView = findViewById(R.id.adView);
            ADS_ADMOB.BannerAd(this, mAdView);
        } else {

            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = findViewById(R.id.banner_container);

            ADS_FACEBOOK.bannerAds(mContext, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));

        }


    }


    private void getDataFromDatabase(String Table_Name) {


        Cursor cursor = new DatabaseHelper(Download_Detail.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, Table_Name).readalldata();
        while (cursor.moveToNext()) {
            int Liked = cursor.getInt(3);
            if (Liked == 1) {
                FirebaseData firebaseData = new FirebaseData(cursor.getInt(0), cursor.getString(1), "cursor.getString(2)", cursor.getString(2), cursor.getInt(3), Table_Name);
                collectonData.add(firebaseData);
            }
        }
        cursor.close();
    }


    private void checkCollectionDataEmpty() {
        if (collectonData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

        }
    }


    private void actionBar() {
        back = findViewById(R.id.back_arrow);
        TextView title = findViewById(R.id.title_collection);
        title.setText("Offline Stories");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

    }


    private void initViews() {
        emptyView = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recyclerView);
    }

}

