package com.bhola.desiKahaniya;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StoryDetails_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> collectonData;

    Context context;
    AlertDialog dialog;

    public StoryDetails_Adapter(List<Object> collectonData, Context context) {
        this.collectonData = collectonData;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View Story_ROW_viewHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout, parent, false);
        return new StoryDetails_Adapter.Story_ROW_viewHolder(Story_ROW_viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        StoryDetails_Adapter.Story_ROW_viewHolder storyRowViewHolder = (Story_ROW_viewHolder) holder;
        FirebaseData firebaseData = (FirebaseData) collectonData.get(position);

        storyRowViewHolder.title.setText(firebaseData.getTitle());
        storyRowViewHolder.date.setText(firebaseData.getDate());


        storyRowViewHolder.recyclerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), StoryPage.class);
                intent.putExtra("_id", firebaseData.getId());
                intent.putExtra("title", firebaseData.getTitle());
                intent.putExtra("Story", firebaseData.getHeading());
                intent.putExtra("date", firebaseData.getDate());
                intent.putExtra("TableName", firebaseData.getTableName());
                intent.putExtra("activityComingFrom", context.getClass().getSimpleName());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });

        storyRowViewHolder.recyclerview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (context.getClass().getSimpleName().equals("Download_Detail")) {
                    final Vibrator vibe = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)


                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());
                    View promptView = inflater.inflate(R.layout.delete, null);
                    builder.setView(promptView);
                    builder.setCancelable(false);
                    Button delete = promptView.findViewById(R.id.DELETE);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final MediaPlayer mp = MediaPlayer.create(v.getContext(), R.raw.sound);
                            mp.start();

                            String[] Table_Names = {"Collection1", "Collection2", "Collection3", "Collection4", "Collection5", "Collection6", "Collection7", "Collection8", "Collection9", "Collection10"};

                            for (int i = 0; i < Table_Names.length; i++) {
                                Cursor cursor = new DatabaseHelper(v.getContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, Table_Names[i]).readalldata();

                                while (cursor.moveToNext()) {
                                    String Title = cursor.getString(2);
                                    if (Title.equals(firebaseData.getTitle())) {
                                        String res = new DatabaseHelper(v.getContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, Table_Names[i]).updaterecord(firebaseData.getId(), 0);
                                        break;
                                    }
                                }
                                cursor.close();
                            }
                            Toast.makeText(v.getContext(), "Removed From Offline Stories ", Toast.LENGTH_SHORT).show();
                            collectonData.remove(position);
                            Download_Detail.adapter2.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    Button cancel = promptView.findViewById(R.id.CANCEL);


                    dialog = builder.create();
                    dialog.show();

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();

                        }
                    });


                    return false;
                }


                return false;
            }
        });


        if (SplashScreen.Ads_State.equals("active")) {
            loadNativeAds(((Story_ROW_viewHolder) holder).template, ((Story_ROW_viewHolder) holder).facebook_BannerAd_layout, holder.getAbsoluteAdapterPosition());
        }
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


    public class Story_ROW_viewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView index, heading, date;
        LinearLayout recyclerview;
        TemplateView template;
        LinearLayout facebook_BannerAd_layout;

        public Story_ROW_viewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerview = itemView.findViewById(R.id.recyclerviewLayout);
            title = itemView.findViewById(R.id.titlee);
            date = itemView.findViewById(R.id.date_recyclerview);
            template = itemView.findViewById(R.id.my_template);
            facebook_BannerAd_layout = itemView.findViewById(R.id.banner_container);

        }
    }
}

