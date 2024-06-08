package com.bhola.desiKahaniya;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineAudioStory extends AppCompatActivity {
    String TAG = "taga";
    List<AudioCategoryModel> collectionData;


    ImageView back, share_ap;
    TextView messageTextview;
    private AdView mAdView;


    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;

    OfflineAudioAdapter adapter;
    RecyclerView recyclerView;
    LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);
//        StatusBarTransparent();

        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }

        actionBar();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        collectionData = new ArrayList<AudioCategoryModel>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSavedAudioFiles();


    }


    private void showAds() {


        if (SplashScreen.Ad_Network_Name.equals("admob")) {
//NO BANNER AD IN STORY LIST

            mAdView = findViewById(R.id.adView);
            ADS_ADMOB.BannerAd(this, mAdView);
        } else {

            LinearLayout facebook_bannerAd_layput;
            facebook_bannerAd_layput = findViewById(R.id.banner_container);
            ADS_FACEBOOK.bannerAds(this, facebook_adView, facebook_bannerAd_layput, getString(R.string.Facebook_BannerAdUnit));
        }

    }


    private void loadSavedAudioFiles() {
        List<String> name_array = new ArrayList<String>();
        List<String> path_array = new ArrayList<String>();

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("Download", Context.MODE_PRIVATE);
        File[] files = directory.listFiles();

        for (File file : files) {
            name_array.add(file.getName());
            path_array.add(file.getPath());
        }


        ArrayList<Object> collectionData = new ArrayList<Object>();
        for (int i = 0; i < name_array.size(); i++) {
            com.bhola.desiKahaniya.AudioOfflineModel model = new com.bhola.desiKahaniya.AudioOfflineModel(name_array.get(i), path_array.get(i));
            collectionData.add(model);
        }
        if (collectionData.size() == 0) {

            messageTextview = findViewById(R.id.message);
            messageTextview.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        recyclerView.setVisibility(View.VISIBLE);
        adapter = new OfflineAudioAdapter(collectionData, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void actionBar() {
        TextView title = findViewById(R.id.title_collection);
        title.setText("Offline Audio Stories");
        title.setTextSize(24);

        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView VipMembership = findViewById(R.id.VipLottie);

        if (SplashScreen.App_updating.equals("active")) {
            VipMembership.setVisibility(View.GONE);
        }
        VipMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SplashScreen.isInternetAvailable(OfflineAudioStory.this)) {
                    startActivity(new Intent(OfflineAudioStory.this, VipMembership.class));

                } else {
                    Toast.makeText(OfflineAudioStory.this, "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void StatusBarTransparent() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public void setWindowFlag(Context activity, final int bits, boolean on) {
        Window win = OfflineAudioStory.this.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(OfflineAudioStory.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(OfflineAudioStory.this, "Please Give Permission to download file", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(OfflineAudioStory.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }


    private boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(OfflineAudioStory.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

}


class OfflineAudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    RewardedInterstitialAd mRewardedVideoAd;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    ArrayList<Object> collectionData = new ArrayList<Object>();


    public OfflineAudioAdapter(ArrayList<Object> data, FragmentActivity activity) {
        this.context = activity;
        this.collectionData = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View Story_ROW_viewHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout, parent, false);
        return new Story_ROW_viewHolder(Story_ROW_viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        com.bhola.desiKahaniya.AudioOfflineModel model = (com.bhola.desiKahaniya.AudioOfflineModel) collectionData.get(holder.getAdapterPosition());
        ((Story_ROW_viewHolder) holder).title.setText(model.getName().replaceAll("_", " ").replace(".mp3", ""));
        ((Story_ROW_viewHolder) holder).title.setTextSize(18);
        ((Story_ROW_viewHolder) holder).delete.setVisibility(View.VISIBLE);

        ((Story_ROW_viewHolder) holder).delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextWrapper cw = new ContextWrapper(v.getContext());
                File directory = cw.getDir("Download", Context.MODE_PRIVATE);
                File file = new File(directory, model.getName());
                if (file.exists()) {
                    file.delete();
                    collectionData.remove(holder.getBindingAdapterPosition());
                    notifyItemRemoved(holder.getBindingAdapterPosition());
                    Toast.makeText(cw, "Story Deleted", Toast.LENGTH_SHORT).show();
                }

            }
        });
        ((Story_ROW_viewHolder) holder).imageview.setImageResource(R.drawable.folder);
        ((Story_ROW_viewHolder) holder).imageview.setPadding(0,5,0,5);
        ((Story_ROW_viewHolder) holder).date.setText("Downloaded");

        ((Story_ROW_viewHolder) holder).recyclerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AudioPlayerOffline.class);
                intent.putExtra("storyURL", model.getPath());
                intent.putExtra("storyName", model.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);

            }
        });


    }



    @Override
    public int getItemCount() {
        return collectionData.size();
    }


    public class Story_ROW_viewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;

        ImageView imageview, delete;
        LinearLayout recyclerview;


        public Story_ROW_viewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerview = itemView.findViewById(R.id.recyclerviewLayout);
            imageview = itemView.findViewById(R.id.imageview);
            delete = itemView.findViewById(R.id.delete);
            title = itemView.findViewById(R.id.titlee);
            date = itemView.findViewById(R.id.date_recyclerview);

        }
    }
}




