package com.bhola.desiKahaniyaAdult;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ftab2 extends Fragment {

    List<String> storyURL;
    List<String> storyName;
    AudioStory_Details_Adapter adapter2;
    StorageReference mStorageReference;
    ProgressBar progressBar2;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
String TAG="TAGA";
    int currentPositonTime;

    public ftab2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ftab2, container, false);

        storyURL = new ArrayList<String>();
        storyName = new ArrayList<String>();
        progressBar2 = view.findViewById(R.id.progressBar2);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("audiostories");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadAudioDatabase(view);

        if (!isInternetAvailable(getContext())) {
            Toast.makeText(getContext(), "Check Internet Connection!", Toast.LENGTH_SHORT).show();
        } else {
        }
        return view;
    }


    private void loadAudioDatabase(View view) {
        ArrayList<Object> collectionData = new ArrayList<Object>();
        Cursor cursor = new DatabaseHelper(getActivity(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readAudioStories();
        try {
            try {
                while (cursor.moveToNext()) {
                    if (cursor.getString(5).trim().length() != 0) {
                        StoryItemModel storyItemModel = new StoryItemModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getInt(9), "cursor.getString(10)", cursor.getInt(11), cursor.getInt(12), cursor.getString(13),cursor.getInt(14));
                        collectionData.add(storyItemModel);
                    }
                }

            } finally {
                cursor.close();
            }

        } catch (Exception ignored) {

        }


        adapter2 = new AudioStory_Details_Adapter(collectionData, getActivity());
        recyclerView.setAdapter(adapter2);
        progressBar2.setVisibility(View.GONE);
        adapter2.notifyDataSetChanged();


    }


    boolean isInternetAvailable(Context context) {
        if (context == null) return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                    Log.i("aaaaaaaaaaaaaaaaa", "" + e.getMessage());
                }
            }
        }
        return false;
    }

}


class AudioStory_Details_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    RewardedInterstitialAd mRewardedVideoAd;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    ArrayList<Object> collectionData = new ArrayList<Object>();


    public AudioStory_Details_Adapter(ArrayList<Object> data, FragmentActivity activity) {
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
        int POSITION = position;

//        AudioModel model = (AudioModel) collectionData.get(position);
        StoryItemModel storyItemModel = (StoryItemModel) collectionData.get(position);

        String filename = SplashScreen.decryption(storyItemModel.getTitle().replace("-", " ").trim());
        ((Story_ROW_viewHolder) holder).imageview.setImageResource(R.drawable.mp3);




        ((Story_ROW_viewHolder) holder).title.setText(filename);
        ((Story_ROW_viewHolder) holder).date.setText(storyItemModel.getDate());
        ((Story_ROW_viewHolder) holder).views.setText(storyItemModel.getViews());

        if (storyItemModel.getRead() == 1) {
            ((Story_ROW_viewHolder) holder).title.setTextColor(Color.parseColor("#9A3412"));
        } else {
            ((Story_ROW_viewHolder) holder).title.setTextColor(Color.parseColor("#374151"));
        }


        ((Story_ROW_viewHolder) holder).recyclerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (isInternetAvailable()) {
                    Intent intent = new Intent(context, AudioPlayer.class);
                    intent.putExtra("storyURL", storyItemModel.getAudiolink());
                    intent.putExtra("storyName", filename);
                    intent.putExtra("title",  storyItemModel.getTitle());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                } else {
                    Toast.makeText(context, "Check Internet Connection!" + System.lineSeparator() +
                            "इंटरनेट कनेक्शन चेक करे", Toast.LENGTH_SHORT).show();
                }

            }

            boolean isInternetAvailable() {
                if (context == null) return false;


                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        });



    }


    @Override
    public int getItemCount() {
        return collectionData.size();
    }


    public class Story_ROW_viewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        TextView views;

        ImageView imageview;
        LinearLayout recyclerview;

        public Story_ROW_viewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerview = itemView.findViewById(R.id.recyclerviewLayout);
            imageview = itemView.findViewById(R.id.imageview);
            title = itemView.findViewById(R.id.titlee);
            date = itemView.findViewById(R.id.date_recyclerview);
            views = itemView.findViewById(R.id.views);

        }
    }
}


