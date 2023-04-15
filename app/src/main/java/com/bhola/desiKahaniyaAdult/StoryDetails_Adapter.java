package com.bhola.desiKahaniyaAdult;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Color;
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




import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StoryDetails_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> collectonData;
    Context context;
    String title_category;
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

        StoryDetails_Adapter.Story_ROW_viewHolder storyRowViewHolder = (StoryDetails_Adapter.Story_ROW_viewHolder) holder;
        StoryItemModel storyItemModel = (StoryItemModel) collectonData.get(position);

        storyRowViewHolder.title.setText(SplashScreen.decryption(storyItemModel.getTitle()));
        storyRowViewHolder.date.setText(storyItemModel.getDate());
        storyRowViewHolder.views.setText(storyItemModel.getViews());
        if (storyItemModel.getRead() == 1) {
            storyRowViewHolder.title.setTextColor(Color.parseColor("#9A3412"));
        } else {
            storyRowViewHolder.title.setTextColor(Color.parseColor("#374151"));
        }


        storyRowViewHolder.recyclerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), StoryPage.class);
                intent.putExtra("category", title_category);
                intent.putExtra("title", SplashScreen.decryption(storyItemModel.getTitle()));
                intent.putExtra("date", storyItemModel.getDate());
                intent.putExtra("href", SplashScreen.decryption(storyItemModel.getHref()));
                intent.putExtra("relatedStories", storyItemModel.getRelatedStories());
                intent.putExtra("storiesInsideParagraph", storyItemModel.getStoriesInsideParagraph());
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

                            String str = (new DatabaseHelper(v.getContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems")).updaterecord(SplashScreen.decryption(storyItemModel.getTitle()), 0);
                            Toast.makeText(v.getContext(), "Removed from Offline Stories" + str, Toast.LENGTH_SHORT).show();
                            collectonData.remove(position);
                            Download_Detail.adapter.notifyDataSetChanged();
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

                }
                return false;


            }
        });


    }




    @Override
    public int getItemCount() {
        return collectonData.size();
    }


    public static class Story_ROW_viewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView index, heading, date, views;
        LinearLayout recyclerview;

        public Story_ROW_viewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerview = itemView.findViewById(R.id.recyclerviewLayout);
            title = itemView.findViewById(R.id.titlee);
            date = itemView.findViewById(R.id.date_recyclerview);
            views = itemView.findViewById(R.id.views);


        }
    }
}

