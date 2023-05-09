package com.bhola.desiKahaniyaAdult2;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class Download_Detail extends AppCompatActivity {


    List<Object> collectonData;
    public static StoryDetails_Adapter adapter;
    ImageView back;
    RecyclerView recyclerView;
    TextView message;
    LinearLayout progressBar;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }


        actionBar();
        initViews();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectonData = new ArrayList<Object>();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getDataFromDatabase();
                checkCollectionDataEmpty();
                adapter.notifyDataSetChanged();

            }
        }, 20);

        adapter = new StoryDetails_Adapter(collectonData, this);
        recyclerView.setAdapter(adapter);


    }

    private void showAds() {

        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            mAdView = findViewById(R.id.adView);
            ADS_ADMOB.BannerAd(this, mAdView);
        } else {

            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = findViewById(R.id.banner_container);

            ADS_FACEBOOK.bannerAds(this, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));

        }


    }

    private void getDataFromDatabase() {

        Cursor cursor = (new DatabaseHelper((Context) this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems")).readLikedStories();
        while (cursor.moveToNext()) {
            StoryItemModel storyItemModel = new StoryItemModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getInt(9), "cursor.getString(10)", cursor.getInt(11), cursor.getInt(12), cursor.getString(13), cursor.getInt(14));
            collectonData.add(storyItemModel);
        }
        cursor.close();

        Cursor cursor2 = (new DatabaseHelper((Context) this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "FakeStory")).readLikedStories();
        while (cursor2.moveToNext()) {
            StoryItemModel storyItemModel = new StoryItemModel(cursor2.getString(0), cursor2.getString(1), cursor2.getString(2), cursor2.getString(3), cursor2.getString(4), cursor2.getString(5), cursor2.getString(6), cursor2.getString(7), cursor2.getString(8), cursor2.getInt(9), "cursor2.getString(10)", cursor2.getInt(11), cursor2.getInt(12), cursor2.getString(13), cursor2.getInt(14));
            collectonData.add(storyItemModel);
        }
        cursor2.close();

        progressBar.setVisibility(View.GONE);


    }


    private void checkCollectionDataEmpty() {
        if (collectonData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);
        }
    }


    private void actionBar() {
        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView title_collection = findViewById(R.id.title_collection);
        title_collection.setText("Offline Stories");


    }


    private void initViews() {
        message = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

    }

}

