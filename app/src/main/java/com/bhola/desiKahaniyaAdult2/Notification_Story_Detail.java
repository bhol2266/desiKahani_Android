package com.bhola.desiKahaniyaAdult2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Notification_Story_Detail extends AppCompatActivity {

    List<Object> collectonData;
    Notification_Story_Adapter adapter2;
    DatabaseReference mref;

    ImageView back, share_ap;
    LinearLayout progressBar;
    String activityComingFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);


        actionBar();
        progressBar = findViewById(R.id.progressBar);
        mref = FirebaseDatabase.getInstance().getReference().child("Notification");
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectonData = new ArrayList<Object>();


        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FirebaseData firebaseData = ds.getValue(FirebaseData.class);
                    collectonData.add(firebaseData);
                }
                Collections.reverse(collectonData);

                if (SplashScreen.App_updating.equals("active")) {
                    collectonData.clear();
                }
                adapter2 = new Notification_Story_Adapter(collectonData, Notification_Story_Detail.this);
                recyclerView.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void actionBar() {
        TextView title = findViewById(R.id.title_collection);
        title.setText("Notifications");

        activityComingFrom = getIntent().getStringExtra("activityComingFrom");

        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(v -> onBackPressed());
        ImageView VipMembership = findViewById(R.id.VipLottie);
        VipMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SplashScreen.isInternetAvailable(Notification_Story_Detail.this)) {
                    if (!SplashScreen.App_updating.equals("active")) {
                        startActivity(new Intent(Notification_Story_Detail.this, VipMembership.class));
                    } else {
                        Toast.makeText(Notification_Story_Detail.this, "coming soon!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Notification_Story_Detail.this, "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),Collection_GridView.class));
        super.onBackPressed();
    }
}


class Notification_Story_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> collectonData;
    Context context;
    AlertDialog dialog;


    public Notification_Story_Adapter(List<Object> collectonData, Context context) {
        this.collectonData = collectonData;
        this.context = context;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View NotificitaionStory_ViewHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout, parent, false);
        return new Notification_Story_Adapter.NotificitaionStory_ViewHolder(NotificitaionStory_ViewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Notification_Story_Adapter.NotificitaionStory_ViewHolder storyRowViewHolder = (Notification_Story_Adapter.NotificitaionStory_ViewHolder) holder;
        FirebaseData storyItemModel = (FirebaseData) collectonData.get(position);

        storyRowViewHolder.title.setText((storyItemModel.getTitle()));
        storyRowViewHolder.date.setText(storyItemModel.getDate());
        storyRowViewHolder.views.setText("3245");


        storyRowViewHolder.recyclerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), StoryPage.class);
                intent.putExtra("title", storyItemModel.getTitle());
                intent.putExtra("story", storyItemModel.getHeading());
                intent.putExtra("date", storyItemModel.getDate());
                intent.putExtra("activityComingFrom", context.getClass().getSimpleName());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });


//        if (SplashScreen.Ads_State.equals("active")) {
//            loadNativeAds(((NotificitaionStory_ViewHolder) holder).template, ((NotificitaionStory_ViewHolder) holder).facebook_BannerAd_layout, holder.getAbsoluteAdapterPosition());
//        }

    }

    private void loadNativeAds(TemplateView template, LinearLayout facebook_BannerAd_layout, int absoluteAdapterPosition) {

        if (SplashScreen.Ad_Network_Name.equals("admob") && absoluteAdapterPosition % SplashScreen.Native_Ad_Interval == 0) {

            template.setVisibility(View.VISIBLE);
            MobileAds.initialize(context);
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.NativeAd))
                            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(NativeAd nativeAd) {

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            NativeTemplateStyle styles = new
                                                    NativeTemplateStyle.Builder().build();
                                            template.setStyles(styles);
                                            template.setNativeAd(nativeAd);
                                        }
                                    });

                                }
                            })
                            .build();
                    adLoader.loadAd(new AdRequest.Builder().build());

                }
            });


        } else {
            template.setVisibility(View.GONE);
        }
        if (SplashScreen.Ad_Network_Name.equals("facebook") && absoluteAdapterPosition % SplashScreen.Native_Ad_Interval == 0) {
            facebook_BannerAd_layout.setVisibility(View.VISIBLE);
            AudienceNetworkAds.initialize(context);

            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Runnable() {
                @Override
                public void run() {
                    com.facebook.ads.AdView facebook_adView = new AdView(context, context.getString(R.string.Facebook_NativeAd_MediumRect), AdSize.BANNER_HEIGHT_50);
                    facebook_adView.loadAd();

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            facebook_BannerAd_layout.addView(facebook_adView);
                        }
                    });
                }
            });


            facebook_BannerAd_layout.setVisibility(View.VISIBLE);
            AudienceNetworkAds.initialize(context);
            AdView adView = new AdView(context, context.getString(R.string.Facebook_NativeAd_MediumRect), AdSize.BANNER_HEIGHT_50);
            facebook_BannerAd_layout.addView(adView);
            adView.loadAd();

        } else {
            facebook_BannerAd_layout.setVisibility(View.GONE);

        }

    }


    @Override
    public int getItemCount() {
        return collectonData.size();
    }


    public class NotificitaionStory_ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView index, heading, date, views;
        LinearLayout recyclerview;
        TemplateView template;
        LinearLayout facebook_BannerAd_layout;
        public NotificitaionStory_ViewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerview = itemView.findViewById(R.id.recyclerviewLayout);
            title = itemView.findViewById(R.id.titlee);
            date = itemView.findViewById(R.id.date_recyclerview);
            views = itemView.findViewById(R.id.views);
            facebook_BannerAd_layout = itemView.findViewById(R.id.banner_container);

        }
    }
}


